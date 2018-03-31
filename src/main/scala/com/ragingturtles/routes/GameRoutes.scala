package com.ragingturtles.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.ragingturtles.services.GameService
import com.ragingturtles.domain.{Game, Move}
import com.ragingturtles.JsonSupport._
import com.softwaremill.session.SessionDirectives.requiredSession
import com.softwaremill.session.SessionManager
import com.softwaremill.session.SessionOptions.{oneOff, usingCookies}

class GameRoutes(gameService: GameService)(implicit sessionManager: SessionManager[String]) {
  def routes =
    requiredSession(oneOff, usingCookies) { sessionUsername =>
      (get & path("games")) {
        val games = gameService.findGames()
        complete(StatusCodes.OK -> games)
      } ~
        (post & path("games")) {
          val game: Game = gameService.createGame(sessionUsername)
          complete(StatusCodes.Created -> game)
        } ~
        (get & path("games" / JavaUUID)) { gameId =>
          gameService.getGame(gameId) match {
            case Some(game) => complete(StatusCodes.OK, game)
            case None => complete(StatusCodes.NotFound)
          }
        } ~
        (put & path("games" / JavaUUID / "join")) { gameId =>
          gameService.joinGame(gameId, sessionUsername) match {
            case Some(game) => complete(StatusCodes.OK, game)
            case None => complete(StatusCodes.NotFound)
          }
        } ~
        (put & path("games" / JavaUUID / "start")) { gameId =>
          gameService.startGame(gameId) match {
            case Some(game) => complete(StatusCodes.OK, game)
            case None => complete(StatusCodes.NotFound)
          }
        } ~
        (put & path("games" / JavaUUID / "move") & entity(as[Move])) { (gameId, move) =>
          gameService.makeMove(gameId, move) match {
            case Some(game) => complete(StatusCodes.OK, game)
            case None => complete(StatusCodes.NotFound)
          }
        }
    }

}
