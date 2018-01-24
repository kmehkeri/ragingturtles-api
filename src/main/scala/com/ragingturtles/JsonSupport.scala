package com.ragingturtles

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.ragingturtles.domain.Actions.Action
import com.ragingturtles.domain.Colors.Color
import com.ragingturtles.domain.GameStatus.{GameStatus, Init}
import com.ragingturtles.domain._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object UUIDFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue) = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw new DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }

  implicit val accountFormat = jsonFormat2(Account)

  implicit val sessionFormat = jsonFormat1(Session)

  implicit object ColorJsonFormat extends JsonFormat[Color] {
    def write(c: Color) = JsString(c.toString)
    def read(value: JsValue) = value match {
      case JsString(v) => Colors.withName(v)
      case _ => throw new DeserializationException("No such color")
    }
  }

  implicit object ActionJsonFormat extends JsonFormat[Action] {
    def write(c: Action) = JsString(c.toString)
    def read(value: JsValue) = value match {
      case JsString(v) => Actions.withName(v)
      case _ => throw new DeserializationException("No such action")
    }
  }

  implicit object GameStatusJsonFormat extends JsonFormat[GameStatus] {
    def write(c: GameStatus) = JsString(c.toString)
    def read(value: JsValue) = value match {
      case JsString(v) => GameStatus.withName(v)
      case _ => throw new DeserializationException("No such action")
    }
  }

  implicit val cardFormat = jsonFormat2(Card)
  implicit val playerFormat = jsonFormat5(Player)
  implicit val turtleFormat = jsonFormat3(Turtle)
  implicit val moveFormat = jsonFormat3(Move)
  implicit val gameFormat = jsonFormat(Game.apply, "id", "players", "turtles", "status", "deck", "log")

}
