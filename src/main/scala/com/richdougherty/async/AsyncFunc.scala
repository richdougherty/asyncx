package com.richdougherty.async

import scala.concurrent.Future

import Async._

trait AsyncFunc[-A,+B] {
  import AsyncFunc._
  def evaluate(a: A): Future[B] = AsyncRuntime.evaluate(apply(a))
  def apply(a: A): Async[B]
  def map[C](f: B => C)(implicit ac: AsyncContext): AsyncFunc[A,C] = Map(this, f, ac)
  def flatMap[C](f: B => AsyncFunc[B,C])(implicit ac: AsyncContext): AsyncFunc[A,C] = FlatMap(this, f, ac)
  def asyncMap[C]: AsyncFunc[AsyncFunc[B,C],AsyncFunc[A,C]] = AsyncMap[A,B,C](this)
  def asyncFlatMap[C]: AsyncFunc[AsyncFunc[B,AsyncFunc[B,C]],AsyncFunc[A,C]] = AsyncFlatMap[A,B,C](this)
}

object AsyncFunc {

  def apply[A,B](f: A => B)(implicit ac: AsyncContext) = Lifted(f, ac)

  def thunked[A,B](f: A => Async[B], ac: AsyncContext) = Thunked(f, ac)

  def direct[A,B](f: A => Async[B]) = Direct(f)

  final case class Lifted[A,B](f: A => B, ac: AsyncContext) extends AsyncFunc[A,B] {
    def apply(a: A) = Thunk(() => Success(f(a)), ac)
  }

  final case class Thunked[A,B](f: A => Async[B], ac: AsyncContext) extends AsyncFunc[A,B] {
    def apply(a: A) = Thunk(() => f(a), ac)
  }

  final case class Direct[A,B](f: A => Async[B]) extends AsyncFunc[A,B] {
    def apply(a: A) = f(a)
  }

  final case class Map[-A,B,+C](x: AsyncFunc[A,B], f: B => C, ac: AsyncContext) extends AsyncFunc[A,C] {
    def apply(a: A) = Thunk(() => x(a).map(f)(ac), TrivialAsyncContext)
  }

  final case class AsyncMap[A,B,C](x: AsyncFunc[A,B]) extends AsyncFunc[AsyncFunc[B,C],AsyncFunc[A,C]] {
    def apply(f: AsyncFunc[B,C]): Async[AsyncFunc[A,C]] = {
      val newFunc = AsyncFunc.direct[A,C] { a =>
        import Implicits.trivial
        for {
          b <- x(a)
          c <- f(b)
        } yield c
      }
      Success(newFunc)
    }
  }

  final case class FlatMap[-A,B,+C](x: AsyncFunc[A,B], f: B => AsyncFunc[B,C], ac: AsyncContext) extends AsyncFunc[A,C] {
    def apply(a: A) = Thunk(() => x(a).flatMap(b => f(b)(b))(ac), TrivialAsyncContext)
  }

  final case class AsyncFlatMap[A,B,C](x: AsyncFunc[A,B]) extends AsyncFunc[AsyncFunc[B,AsyncFunc[B,C]],AsyncFunc[A,C]] {
    def apply(f: AsyncFunc[B,AsyncFunc[B,C]]): Async[AsyncFunc[A,C]] = {
      val newFunc = AsyncFunc.direct[A,C] { (a: A) =>
        import Implicits.trivial
        for {
          b <- x(a)
          bfunc <- f(b)
          c <- bfunc(b)
        } yield c
      }
      Success(newFunc)
    }
  }

}