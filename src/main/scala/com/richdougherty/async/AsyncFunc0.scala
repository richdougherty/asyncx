package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.Future

trait AsyncFunc0[+A] extends (() => Future[A]) {
  import AsyncFunc0._
  def apply(): Future[A] = AsyncRuntime.evaluate(step)
  def step: Step[A]
  def map[B](f: A => B)(implicit ac: AsyncContext): AsyncFunc0[B] = asyncMap(AsyncFunc1.Lifted(f, ac))
  def flatMap[B](f: A => AsyncFunc0[B])(implicit ac: AsyncContext): AsyncFunc0[B] = asyncFlatMap(AsyncFunc1.Lifted(f, ac))
  def asyncMap[B](f: AsyncFunc1[A, B]): AsyncFunc0[B] = Map(this, f)
  def asyncFlatMap[B](f: AsyncFunc1[A, AsyncFunc0[B]]): AsyncFunc0[B] = FlatMap(this, f)
}

object AsyncFunc0 {

  def apply[A](f: () => A)(implicit ac: AsyncContext) = Lifted(f, ac)

  final case class Lifted[+A](f: () => A, ac: AsyncContext) extends AsyncFunc0[A] {
    def step = AsyncThunk(() => Result(f()), ac)
  }

  final case class Map[A,+B](x: AsyncFunc0[A], y: AsyncFunc1[A,B]) extends AsyncFunc0[B] {
    def step = TrivialThunk(() => x.step.flatMap(a => y.step(a)))
  }

  final case class FlatMap[A,+B](x: AsyncFunc0[A], y: AsyncFunc1[A,AsyncFunc0[B]]) extends AsyncFunc0[B] {
    def step = TrivialThunk(() => x.step.flatMap(a => y.step(a).flatMap(z => z.step)))
  }

}