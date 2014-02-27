package com.richdougherty.async.iter

import com.richdougherty.async._

object AsyncIteration {

  final case class Result[A,B](iterator: Option[AsyncIterator[A]], iteratee: AsyncIteratee[A,B]) {
    def value: Option[B] = iteratee match {
      case AsyncIteratee.Done(value) => Some(value)
      case AsyncIteratee.Accepting(_) => None
    }
  }

  def iterate[A,B](iterator: AsyncIterator[A], iteratee: AsyncIteratee[A,B]): Async[Result[A,B]] = Async.Thunk({ () =>
    import Implicits.trivial
    (iterator, iteratee) match {

      case (AsyncIterator.Element(value, next), AsyncIteratee.Accepting(accept)) =>
        for {
          nextIteratee <- accept(Step.Element(value))
          nextIterator <- next
          result <- iterate(nextIterator, nextIteratee)
        } yield result

      case (AsyncIterator.End, AsyncIteratee.Accepting(accept)) =>
        for {
          nextIteratee <- accept(Step.End)
        } yield Result(None, nextIteratee)

      case (_, AsyncIteratee.Done(_)) =>
        Async.Success(Result(Some(iterator), iteratee))

    }
  }, TrivialAsyncContext)

}
