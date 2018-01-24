package com.ragingturtles.domain

/** Enumeration of card colors */
object Colors extends Enumeration {
  type Color = Value

  val Red = Value("R")
  val Green = Value("G")
  val Blue = Value("B")
  val Purple = Value("P")
  val Yellow = Value("Y")
  val Last = Value("$")
  val Wild = Value("*")

  def baseValues = values filter (v => v != Last && v != Wild)
}

/** Enumeration of card actions */
object Actions extends Enumeration {
  type Action = Value

  val Forward = Value("+")
  val Forward2 = Value("++")
  val Back = Value("-")

  def moveDelta(action: Action): Int = action match {
    case Forward => +1
    case Forward2 => +2
    case Back => -1
  }
}

import com.ragingturtles.domain.Actions._
import com.ragingturtles.domain.Colors._

/** Represents cards */
case class Card(color: Color, action: Action)

/** Standard card deck */
object Deck {
  val cards: List[Card] =
    Colors.baseValues.toList.flatMap(color => List.fill(3)(Card(color, Forward)) ++ List.fill(2)(Card(color, Forward2)) ++ List.fill(2)(Card(color, Back))) ++
      List.fill(6)(Card(Last, Forward)) ++
      List.fill(3)(Card(Last, Forward2)) ++
      List.fill(4)(Card(Wild, Forward)) ++
      List.fill(2)(Card(Wild, Forward2))
}
