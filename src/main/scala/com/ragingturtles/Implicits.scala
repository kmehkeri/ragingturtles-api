package com.ragingturtles

object Implicits {
  /** Provide .zipped method for 4 lists */
  implicit class Tuple4Zipped[A, B, C, D](val t: (Iterable[A], Iterable[B], Iterable[C], Iterable[D])) extends AnyVal {
    def zipped = t._1.toStream
      .zip(t._2).zip(t._3).zip(t._4)
      .map { case (((a, b), c), d) => (a, b, c, d) }
  }

  /** Provide toWithin method for Ordered */
  class PimpedOrdered(val v: Int) {
    def toWithin(min: Int, max: Int): Int =
      if (v < min) min else if (v > max) max else v
  }

  implicit def pimpMyOrdered(v: Int): PimpedOrdered =
    new PimpedOrdered(v)
}