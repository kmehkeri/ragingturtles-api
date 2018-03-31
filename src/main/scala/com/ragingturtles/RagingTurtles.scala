package com.ragingturtles

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.ragingturtles.repositories._
import com.ragingturtles.routes._
import com.ragingturtles.services._
import com.softwaremill.session.{SessionConfig, SessionManager}

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

  // Setup services
  val accountService = new AccountService
  val gameService = new GameService(new InMemoryGameRepository)

  // Routes
  val routes = new AccountRoutes(accountService).routes ~ new GameRoutes(gameService).routes

  // Run server
  val bindingFuture = Http().bindAndHandle(routes, httpHost, httpPort)

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
