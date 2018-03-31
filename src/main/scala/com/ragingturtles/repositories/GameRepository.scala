package com.ragingturtles.repositories

import java.util.UUID

import com.ragingturtles.domain.Game

/** Trait to persist game information */
trait GameRepository {
  /** Return all games */
  def all: Array[Game]

  /** Find game by its id */
  def findById(id: UUID): Option[Game]

  /** Find all games of a user */
  def findByUsername(username: String): Array[Game]

  /** Save the game (update if exists, insert otherwise) */
  def save(game: Game): Unit

  /** Find game, run an action on it and save back */
  def act(id: UUID)(action: Game => Game): Option[Game]
}

/** Simple in-memory implementation of game repository */
class InMemoryGameRepository extends GameRepository {
  private var games: Array[Game] = Array()

  def all: Array[Game] = games

  def findById(id: UUID): Option[Game] = games.find(_.id == id)

  def findByUsername(username: String): Array[Game] = games.filter(_.players.map(_.username).contains(username))

  def save(game: Game): Unit = {
    val i = games.indexWhere(_.id == game.id)
    if (i == -1) games :+= game else games.update(i, game)
  }

  def act(id: UUID)(action: Game => Game): Option[Game] = {
    findById(id).map { game =>
      val updatedGame = action(game)
      save(updatedGame)
      updatedGame
    }
  }
}
