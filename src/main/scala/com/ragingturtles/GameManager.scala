package com.ragingturtles

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import com.ragingturtles.domain.{Game, Move}

case class FindGames(username: Option[String])
case class GetGame(gameId: UUID)
case class CreateGame(leader: String)
case class JoinGame(game: Game, username: String)
case class StartGame(game: Game)
case class MakeMove(game: Game, move: Move)

class GameManager(gameRepository: GameRepository) extends Actor with ActorLogging {
  def receive = {
    case GetGame(gameId) =>
      val game = gameRepository.findById(gameId)
      sender ! game

    case CreateGame(leader) =>
      val game = Game.createFor(leader)
      gameRepository.save(game)
      log.info(s"Game created with leader ${leader}")
      sender ! game

    case JoinGame(game, username) =>
      ???

    case StartGame(game) =>
      ???

    case MakeMove(game, move) =>
      ???

  }

  private def gameActor(game: Game) =
    ???
}
