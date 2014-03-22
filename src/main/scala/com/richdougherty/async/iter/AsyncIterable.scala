package com.richdougherty.async.iter

import com.richdougherty.async._

trait AsyncIterable[A] {
  def asyncIterator: Async[AsyncIterator[A]]
  def asyncIterate[B](iteratee: AsyncIteratee[A,B]): Async[Option[B]] = {
    import Implicits.trivial
    asyncIterator.flatMap(iterator => AsyncIteration.iterate(iterator, iteratee)).map(_.value)
  }
}

object AsyncIterable {
  def fromIterable[A](s: Iterable[A])(implicit ac: AsyncContext): AsyncIterable[A] = new AsyncIterable[A] {
    def asyncIterator = Async(s.iterator).flatMap(AsyncIterator.fromIterator(_))(TrivialAsyncContext)
  }
}