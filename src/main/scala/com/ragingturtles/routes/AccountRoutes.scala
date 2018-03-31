package com.ragingturtles.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.ragingturtles.services.AccountService
import com.ragingturtles.domain.{Account, Session}
import com.ragingturtles.JsonSupport._
import com.softwaremill.session.SessionDirectives.{invalidateSession, requiredSession, setSession}
import com.softwaremill.session.SessionManager
import com.softwaremill.session.SessionOptions.{oneOff, usingCookies}

class AccountRoutes(accountService: AccountService)(implicit sessionManager: SessionManager[String]) {
  def routes =
    // Registration
    (post & path("account") & entity(as[Account])) { account =>
      val registeredAccount = accountService.createAccountForCredentials(account.username, account.password)
      complete(StatusCodes.Created)
    } ~
      // Authentication
      (post & path("session") & entity(as[Account])) { account =>
        if (accountService.authenticateUsingPassword(account.username, account.password)) {
          setSession(oneOff, usingCookies, account.username) {
            complete(StatusCodes.OK)
          }
        } else
          complete(StatusCodes.Unauthorized -> "Login failed")
      } ~
      (get & path("session")) {
        requiredSession(oneOff, usingCookies) { sessionUsername =>
          complete(Session(sessionUsername))
        }
      } ~
      (delete & path("session")) {
        requiredSession(oneOff, usingCookies) { sessionUsername =>
          invalidateSession(oneOff, usingCookies) {
            complete(StatusCodes.OK)
          }
        }
      }
}
