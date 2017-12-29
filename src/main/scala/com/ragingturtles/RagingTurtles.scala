package com.ragingturtles

import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.pattern.ask
import akka.util.Timeout
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._

import concurrent.duration._
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val accountFormat = jsonFormat2(Account)
  implicit val sessionFormat = jsonFormat1(Session)
}

object RagingTurtles extends App with Config with JsonSupport {
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

  // Routes
  val route =
    // Root
    get {
      pathSingleSlash {
        complete(StatusCodes.OK)
      }
    } ~
    // Registration
    post {
      path("account") {
        entity(as[Account]) { account =>
          val createAccountF = accountManager ? CreateAccount(account)
          onSuccess(createAccountF) {
            case reply => complete(StatusCodes.Created)
          }
        }
      }
    } ~
    // Authentication
    path("session") {
      post {
        entity(as[Account]) { account =>
          log.info(s"Logging in as ${account.username}")
          setSession(oneOff, usingCookies, account.username) {
            complete(StatusCodes.OK)
          }
        }
      } ~
      get {
        requiredSession(oneOff, usingCookies) { sessionUsername =>
          complete(Session(sessionUsername))
        }
      } ~
      delete {
        requiredSession(oneOff, usingCookies) { sessionUsername =>
          log.info(s"Logging out ${sessionUsername}")
          invalidateSession(oneOff, usingCookies) {
            complete(StatusCodes.OK)
          }
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
