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
import concurrent.duration._
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val accountFormat = jsonFormat2(Account)
}

object RagingTurtles extends App with Config with JsonSupport {
  implicit val actorSystem = ActorSystem("raging_turtles")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val log = Logging.getLogger(actorSystem, this)

  val accountManager = actorSystem.actorOf(Props[AccountManager], "account_manager")

  val route =
    get {
      pathSingleSlash {
        complete(StatusCodes.OK)
      }
    } ~
    post {
      path("accounts") {
        entity(as[Account]) { account =>
          val createAccountF = accountManager ? CreateAccount(account)
          onSuccess(createAccountF) {
            case reply => complete(StatusCodes.Created)
          }
        }
      }
    }

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
