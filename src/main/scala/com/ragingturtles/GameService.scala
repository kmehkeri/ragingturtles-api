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

  def joinGame(game: Game, username: String) = ???

  def startGame(game: Game) = ???

  def makeMove(game: Game, move: Move) = ???

}
