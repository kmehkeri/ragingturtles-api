package com.ragingturtles

import akka.actor.Actor
import akka.actor.ActorLogging

final case class CreateAccount(account: Account)

class AccountManager extends Actor with ActorLogging {
  def receive = {
    case CreateAccount(account) =>
      log.info(s"Create account ${account.username}/${account.password}")
      sender ! true
  }
}
