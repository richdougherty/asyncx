package com.richdougherty.iter

import scala.annotation.tailrec

object Iteration {

  final case class Result[A,B](iterator: Option[Iterator[A]], iteratee: Iteratee[A,B]) {
    def value: Option[B] = iteratee match {
      case Iteratee.Done(value) => Some(value)
      case Iteratee.Accepting(_) => None
    }
  }

  def iterate[A,B](iterable: Iterable[A], iteratee: Iteratee[A,B]): Result[A,B] =
    iterate(iterable.iterator, iteratee)

  @tailrec
  def iterate[A,B](iterator: Iterator[A], iteratee: Iteratee[A,B]): Result[A,B] = {
    (iterator, iteratee) match {
      case (Iterator.Element(value, next), Iteratee.Accepting(accept)) =>
        val nextIterator = next()
        val nextIteratee = accept(Step.Element(value))
        iterate(nextIterator, nextIteratee)
      case (Iterator.End, Iteratee.Accepting(accept)) =>
        val nextIteratee = accept(Step.End)
        Result(None, nextIteratee)
      case (_, Iteratee.Done(_)) =>
        Result(Some(iterator), iteratee)
    }
  }
}
