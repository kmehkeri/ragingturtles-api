package com.ragingturtles

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.ragingturtles.domain.Actions.Action
import com.ragingturtles.domain.Colors.Color
import com.ragingturtles.domain.{Account, Actions, Colors, Session}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}

object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val accountFormat = jsonFormat2(Account)

  implicit val sessionFormat = jsonFormat1(Session)

  implicit object ColorJsonFormat extends JsonFormat[Color] {
    def write(c: Color) = JsString(c.toString)
    def read(value: JsValue) = value match {
      case JsString(v) => Colors.withName(v)
      case _ => throw new IllegalArgumentException("No such color")
    }
  }

  implicit object ActionJsonFormat extends JsonFormat[Action] {
    def write(c: Action) = JsString(c.toString)
    def read(value: JsValue) = value match {
      case JsString(v) => Actions.withName(v)
      case _ => throw new IllegalArgumentException("No such action")
    }
  }
}
