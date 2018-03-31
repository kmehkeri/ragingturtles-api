package com.ragingturtles.services

import com.ragingturtles.domain.Account

class AccountService {
  def createAccountForCredentials(username: String, password: String): Account = {
    require(username.length > 0 && password.length > 0)
    Account(username, password)
  }

  def authenticateUsingPassword(username: String, password: String): Boolean =
    username.length > 0 && password.length > 0
}
