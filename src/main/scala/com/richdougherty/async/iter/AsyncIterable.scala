package com.richdougherty.async.iter

import com.richdougherty.async._

trait AsyncIterable[A] {
  def asyncIterator: Async[AsyncIterator[A]]
  def asyncIterate[B](iteratee: AsyncIteratee[A,B]): Async[AsyncIteration.Result[A,B]] = {
    asyncIterator.flatMap(iterator => AsyncIteration.iterate(iterator, iteratee))(TrivialAsyncContext)
  }
}

object AsyncIterable {
  def fromIterable[A](s: Iterable[A])(implicit ac: AsyncContext): AsyncIterable[A] = new AsyncIterable[A] {
    def asyncIterator = Async(s.iterator).flatMap(AsyncIterator.fromIterator(_))(TrivialAsyncContext)
  }
}