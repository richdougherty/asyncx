package com.richdougherty.iter

import scala.annotation.tailrec

trait Iterable[+A] {
  def iterator: Iterator[A]
  def iterate[B](iteratee: Iteratee[A,B]): Option[B] = Iteration.iterate(this, iteratee).value
}
