package com.ragingturtles

/** Enumeration of card colors */
object Color extends Enumeration {
  type Color = Value
  val Red, Green, Blue, Purple, Yellow, Last, Wild = Value
  def baseValues = values filter (v => v != Last && v != Wild)
}

/** Enumeration of card actions */
object Action extends Enumeration {
  type Action = Value
  val Forward, Forward2, Back = Value
  def moveDelta(action: Action): Int = action match {
    case Forward => +1
    case Forward2 => +2
    case Back => -1
  }
}

import Color._
import Action._

/** Represents cards */
case class Card(color: Color, action: Action)

/** Standard card deck */
object Deck {
  val cards: List[Card] =
    Color.baseValues.toList.flatMap(color => List.fill(3)(Card(color, Forward)) ++ List.fill(2)(Card(color, Forward2)) ++ List.fill(2)(Card(color, Back))) ++
      List.fill(6)(Card(Last, Forward)) ++
      List.fill(3)(Card(Last, Forward2)) ++
      List.fill(4)(Card(Wild, Forward)) ++
      List.fill(2)(Card(Wild, Forward2))
}
