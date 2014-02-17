package com.richdougherty.async

import scala.concurrent.Future

import Async._

trait AsyncFunc[-A,+B] {
  import AsyncFunc._
  def evaluate(a: A): Future[B] = AsyncRuntime.evaluate(apply(a))
  def apply(a: A): Async[B]
  def map[C](f: B => C)(implicit ac: AsyncContext): AsyncFunc[A,C] = Map(this, f, ac)
  def flatMap[C](f: B => AsyncFunc[B,C])(implicit ac: AsyncContext): AsyncFunc[A,C] = FlatMap(this, f, ac)
  // def asyncMap[C]: AsyncFunc[AsyncFunc[B,C],AsyncFunc[A,C]] = AsyncMap[A,B,C]()
  // def asyncFlatMap[C]: AsyncFunc[AsyncFunc[B,AsyncFunc[B,C]],AsyncFunc[A,C]] = TrivialLifted(f => FlatMap(this, f))
  // def curry(a: A): Async[B] = Curry(this, a)
}

object AsyncFunc {

  def apply[A,B](f: A => B)(implicit ac: AsyncContext) = Lifted(f, ac)

  final case class Lifted[A,B](f: A => B, ac: AsyncContext) extends AsyncFunc[A,B] {
    def apply(a: A) = Thunk(() => Success(f(a)), ac)
  }

  // final case class TrivialLifted[A,B](f: A => B) extends AsyncFunc[A,B] {
  //   def step(a: A) = TrivialThunk(() => Result(f(a)))
  // }

  final case class Map[-A,B,+C](x: AsyncFunc[A,B], f: B => C, ac: AsyncContext) extends AsyncFunc[A,C] {
    def apply(a: A) = Thunk(() => x(a).map(f)(ac), TrivialAsyncContext)
  }

  // final case class AsyncMap[A,B,C]() extends AsyncFunc[AsyncFunc[B,C],AsyncFunc[A,C]] {
  //   def apply(a: A) = Thunk(() => x(a).map(flatMap(b => y.step(b)), )
  // }

  final case class FlatMap[-A,B,+C](x: AsyncFunc[A,B], f: B => AsyncFunc[B,C], ac: AsyncContext) extends AsyncFunc[A,C] {
    def apply(a: A) = Thunk(() => x(a).flatMap(b => f(b)(b))(ac), TrivialAsyncContext)
  }

  // final case class FlatMap[-A,B,+C](x: AsyncFunc[A,B], y: AsyncFunc[B,AsyncFunc[B,C]]) extends AsyncFunc[A,C] {
  //   def step(a: A) = TrivialThunk(() => x.step(a).flatMap(b => y.step(b).flatMap(z => z.step(b))))
  // }

  // final case class Curry[A,+B](x: AsyncFunc[A,B], a: A) extends Async[B] {
  //   def step = x.step(a)
  // }

}