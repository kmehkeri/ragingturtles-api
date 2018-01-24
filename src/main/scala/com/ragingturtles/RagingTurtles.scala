package com.ragingturtles

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.ragingturtles.domain._
import com.ragingturtles.JsonSupport._
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._

object RagingTurtles extends App with Config {
  // Akka / actor system configuration
  implicit val actorSystem = ActorSystem("raging_turtles")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  implicit val timeout = Timeout(5 seconds)

  // Logging
  val log = Logging.getLogger(actorSystem, this)

  // Authentication setup
  val sessionConfig = SessionConfig.default(serverSecret)
  implicit val sessionManager = new SessionManager[String](sessionConfig)

  // Setup actors
  val accountManager = actorSystem.actorOf(Props[AccountManager], "account_manager")
  val gameService = new GameService(new InMemoryGameRepository)

  // Routes
  val route =
    // Registration
    (post & path("account") & entity(as[Account])) { account =>
      val createAccountF = accountManager ? CreateAccount(account)
      onSuccess(createAccountF) {
        case _ => complete(StatusCodes.Created)
      }
    } ~
      // Authentication
      (post & path("session") & entity(as[Account])) { account =>
        log.info(s"Logging in as ${account.username}")
        setSession(oneOff, usingCookies, account.username) {
          complete(StatusCodes.OK)
        }
      } ~
      (get & path("session")) {
        requiredSession(oneOff, usingCookies) { sessionUsername =>
          complete(Session(sessionUsername))
        }
      } ~
      (delete & path("session")) {
        requiredSession(oneOff, usingCookies) { sessionUsername =>
          log.info(s"Logging out ${sessionUsername}")
          invalidateSession(oneOff, usingCookies) {
            complete(StatusCodes.OK)
          }
        }
      } ~
      // Games
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
            requiredSession(oneOff, usingCookies) { sessionUsername =>
              complete(StatusCodes.OK)
            }
          } ~
          (put & path("games" / JavaUUID / "start")) { gameId =>
            requiredSession(oneOff, usingCookies) { sessionUsername =>
              complete(StatusCodes.OK)
            }
          } ~
          (put & path("games" / JavaUUID / "turn")) { gameId =>
            requiredSession(oneOff, usingCookies) { sessionUsername =>
              complete(StatusCodes.OK)
            }
          }
      }

  // Run server
  val bindingFuture = Http().bindAndHandle(route, httpHost, httpPort)

  bindingFuture.onComplete {
    case Success(b) => {
      log.info(s"Server started at ${httpHost}:${httpPort}")

      // If readLine returns end-of-stream it means we are (most probably) in non-interactive mode,
      // so don't do anything and wait to be killed
      if (scala.io.StdIn.readLine != null) {
        // Otherwise terminate, we're inside sbt maybe and user pressed ENTER
        log.info("Terminating")
        b.unbind().onComplete { _ =>
          actorSystem.terminate()
        }
      }
    }
    case Failure(e) => {
      println("Aborting")
      actorSystem.terminate()
    }
  }
}
