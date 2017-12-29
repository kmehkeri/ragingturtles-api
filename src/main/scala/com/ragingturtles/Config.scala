package com.ragingturtles

import com.typesafe.config.ConfigFactory

trait Config {
  protected val config = ConfigFactory.load()

  protected val httpHost = config.getString("http.host")
  protected val httpPort = config.getInt("http.port")
  protected val serverSecret = config.getString("auth.serverSecret")
}