package com.richdougherty.async

import scala.concurrent.{ ExecutionContext, Future }

trait AsyncFunc1[-A,+B] extends (A => Future[B]) {
  import AsyncFunc1._
  def apply(a: A): Future[B] = AsyncRuntime.evaluate(step(a))
  def step(a: A): Step[B]
  def map[C](f: B => C)(implicit ac: AsyncContext): AsyncFunc1[A,C] = asyncMap(Lifted(f, ac))
  def flatMap[C](f: B => AsyncFunc1[B,C])(implicit ac: AsyncContext): AsyncFunc1[A,C] = asyncFlatMap(Lifted(f, ac))
  def asyncMap[C](f: AsyncFunc1[B,C]): AsyncFunc1[A,C] = Map(this, f)
  def asyncFlatMap[C](f: AsyncFunc1[B, AsyncFunc1[B, C]]): AsyncFunc1[A,C] = FlatMap(this, f)
}

object AsyncFunc1 {

  def apply[A,B](f: A => B)(implicit ac: AsyncContext) = Lifted(f, ac)

  final case class Lifted[A,B](f: A => B, ac: AsyncContext) extends AsyncFunc1[A,B] {
    def step(a: A) = AsyncThunk(() => Result(f(a)), ac)
  }

  final case class Map[-A,B,+C](x: AsyncFunc1[A,B], y: AsyncFunc1[B,C]) extends AsyncFunc1[A,C] {
    def step(a: A) = TrivialThunk(() => x.step(a).flatMap(b => y.step(b)))
  }

  final case class FlatMap[-A,B,+C](x: AsyncFunc1[A,B], y: AsyncFunc1[B,AsyncFunc1[B,C]]) extends AsyncFunc1[A,C] {
    def step(a: A) = TrivialThunk(() => x.step(a).flatMap(b => y.step(b).flatMap(z => z.step(b))))
  }

}