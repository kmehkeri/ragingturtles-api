package com.ragingturtles

import java.util.UUID

import com.ragingturtles.domain.{Game, Move}

class GameService(gameRepository: GameRepository) {
  def findGames(username: Option[String] = None): Array[Game] =
    gameRepository.all

  def getGame(gameId: UUID): Option[Game] =
    gameRepository.findById(gameId)

  def createGame(leader: String): Game = {
    val game = Game.createFor(leader)
    gameRepository.save(game)
    game
  }

  def joinGame(gameId: UUID, username: String): Option[Game] =
    gameRepository.act(gameId) { game => game.join(username) }

  def startGame(gameId: UUID): Option[Game] =
    gameRepository.act(gameId) { game => game.start }

  def makeMove(gameId: UUID, move: Move): Option[Game] =
    gameRepository.act(gameId) { game => game.makeMove(move) }

}
