package com.ragingturtles.domain

import java.util.UUID

import com.ragingturtles.domain.Actions._
import com.ragingturtles.domain.Colors._
import com.ragingturtles.Implicits._

import scala.util.{Random, Try}

/** Represents a player taking part in game */
case class Player(username: String, isLeader: Boolean, color: Option[Color] = None, isActive: Boolean = false,
                  cards: List[Card] = List()) {

  /** Initialize player, assigning the color, handing out starting cards and making active if starting */
  def initialize(assignedColor: Color, startingCards: List[Card], isStartingPlayer: Boolean): Player = {
    Player(username, isLeader, Some(assignedColor), isStartingPlayer, startingCards)
  }

  /** Make a move */
  def makeMove(card: Card, replacementCard: Card): Player = {
    require(isActive)
    require(cards.contains(card))
    Player(username, isLeader, color, false, (cards diff List(card)) :+ replacementCard)
  }

  /** Make this player active */
  def makeActive: Player = {
    require(!isActive)
    Player(username, isLeader, color, true, cards)
  }
}

/** Represents a turtle on the board */
case class Turtle(color: Color, position: Int = 0, height: Int = 0)

/** Represents a move in the game */
case class Move(username: String, card: Card, color: Color)

/** Enumeration of possible game statuses */
object GameStatus extends Enumeration {
  type GameStatus = Value
  val Init, Playing, Completed = Value
}

import com.ragingturtles.domain.GameStatus._

/** Main game class */
case class Game(id: UUID, players: List[Player], turtles: List[Turtle] = Nil, status: GameStatus = Init,
                deck: List[Card] = List(), log: List[Move] = List()) {
  val MIN_PLAYERS = 2
  val MAX_PLAYERS = 5
  val STARTING_CARDS = 5
  val WINNING_POSITION = 9

  /** Join a player */
  def join(username: String): Game = {
    require(status == Init)
    require(players.length < MAX_PLAYERS)
    Game(id, players :+ Player(username, false))
  }

  /** Start the game */
  def start: Game = {
    require(players.length >= MIN_PLAYERS)

    val colorAssignments = Random.shuffle(Colors.baseValues.toList)
    val deck = Random.shuffle(Deck.cards)
    val startingCardSets = deck.take(players.length * STARTING_CARDS).grouped(STARTING_CARDS).toList
    val startingPlayerFlags = Random.shuffle(true :: List.fill(players.length - 1)(false))
    val initializedPlayers = (players, colorAssignments, startingCardSets, startingPlayerFlags).zipped.toList.map {
      case (player, assignedColor, startingCards, isStartingPlayer) => player.initialize(assignedColor, startingCards, isStartingPlayer)
    }
    val turtles = Colors.baseValues.toList.map(Turtle(_))

    Game(id, initializedPlayers, turtles, Playing, deck.drop(players.length * STARTING_CARDS))
  }

  /** Check whether a move is valid */
  def requireValidMove(move: Move): Unit = {
    require(status == Playing)
    require(move.username == activePlayer.username)
    require(activePlayer.cards.contains(move.card))
    require(Colors.baseValues.contains(move.color))
    require(move.card.color == Wild || (move.card.color == Last && lastTurtles.map(_.color).contains(move.color)) || move.card.color == move.color)
  }

  /** Make a move */
  def makeMove(move: Move): Game = {
    requireValidMove(move)

    // Move the turtles
    val selectedTurtle = turtles.find(_.color == move.color).get
    val targetPosition = selectedTurtle.position + moveDelta(move.card.action).toWithin(0, WINNING_POSITION)
    val targetHeight = Try(turtles.filter(_.position == targetPosition).map(_.height).max + 1).getOrElse(0)
    val (touchedTurtles, remainingTurtles) =
      turtles.partition(t => t == selectedTurtle || (t.position == selectedTurtle.position && t.height > selectedTurtle.height))
    val updatedTurtles = touchedTurtles.sortBy(_.height).zipWithIndex.map{ case (turtle, i) => Turtle(turtle.color, targetPosition, if (targetPosition > 0) targetHeight + i else 0) } ++ remainingTurtles

    // Update players
    val updatedPlayers = players.updated(players.indexOf(activePlayer), activePlayer.makeMove(move.card, deck.head))
                                .updated(players.indexOf(nextPlayer), nextPlayer.makeActive)

    // Update deck
    val updatedDeck = if (deck.tail.isEmpty) reshuffledDeck else deck.tail

    // Finally return updated game
    Game(id, updatedPlayers, updatedTurtles, Playing, updatedDeck, log :+ move)
  }

  /** Checks for winner and completes the game if found */
  def checkWinner = ???

  /** Returns active player */
  private lazy val activePlayer: Player = {
    require(status == Playing)
    players.find(_.isActive).get
  }

  /** Returns next player to move after the active one */
  private lazy val nextPlayer: Player = {
    require(status == Playing)
    players((players.indexOf(activePlayer) + 1) % players.length)
  }

  private lazy val lastTurtles: List[Turtle] = {
    require(status == Playing)
    turtles.filter(_.position == turtles.map(_.position).min)
  }

  /** Reshuffle deck when it contains last card (it will be used on current move leaving deck empty) */
  private def reshuffledDeck: List[Card] = {
    require(status == Playing)
    require(deck.tail.isEmpty)
    Random.shuffle(Deck.cards diff (deck.head :: players.flatMap(_.cards)))
  }
}

object Game {
  /** Create a game with supplied username as leader */
  def createFor(leader: String): Game =
    Game(UUID.randomUUID, List(Player(leader, true)))
}
