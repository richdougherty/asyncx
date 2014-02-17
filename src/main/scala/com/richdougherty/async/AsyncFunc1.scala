package com.richdougherty.async

import scala.concurrent.{ ExecutionContext, Future }

trait AsyncFunc1[-A,+B] extends (A => Future[B]) {
  import AsyncFunc1._
  def apply(a: A): Future[B] = AsyncRuntime.evaluate(step(a))
  def step(a: A): Step[B]
  def map[C](f: B => C)(implicit ac: AsyncContext): AsyncFunc1[A,C] = Map(this, Lifted(f, ac))
  def flatMap[C](f: B => AsyncFunc1[B,C])(implicit ac: AsyncContext): AsyncFunc1[A,C] = FlatMap(this, Lifted(f, ac))
  def asyncMap[C]: AsyncFunc1[AsyncFunc1[B,C],AsyncFunc1[A,C]] = TrivialLifted(f => Map(this, f))
  def asyncFlatMap[C]: AsyncFunc1[AsyncFunc1[B,AsyncFunc1[B,C]],AsyncFunc1[A,C]] = TrivialLifted(f => FlatMap(this, f))
  def curry(a: A): AsyncFunc0[B] = Curry(this, a)
}

object AsyncFunc1 {

  def apply[A,B](f: A => B)(implicit ac: AsyncContext) = Lifted(f, ac)

  final case class Lifted[A,B](f: A => B, ac: AsyncContext) extends AsyncFunc1[A,B] {
    def step(a: A) = AsyncThunk(() => Result(f(a)), ac)
  }

  final case class TrivialLifted[A,B](f: A => B) extends AsyncFunc1[A,B] {
    def step(a: A) = TrivialThunk(() => Result(f(a)))
  }

  final case class Map[-A,B,+C](x: AsyncFunc1[A,B], y: AsyncFunc1[B,C]) extends AsyncFunc1[A,C] {
    def step(a: A) = TrivialThunk(() => x.step(a).flatMap(b => y.step(b)))
  }

  final case class FlatMap[-A,B,+C](x: AsyncFunc1[A,B], y: AsyncFunc1[B,AsyncFunc1[B,C]]) extends AsyncFunc1[A,C] {
    def step(a: A) = TrivialThunk(() => x.step(a).flatMap(b => y.step(b).flatMap(z => z.step(b))))
  }

  final case class Curry[A,+B](x: AsyncFunc1[A,B], a: A) extends AsyncFunc0[B] {
    def step = x.step(a)
  }

}