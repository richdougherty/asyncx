package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.Future

trait AsyncFunc0[+A] extends (() => Future[A]) {
  import AsyncFunc0._
  def apply(): Future[A] = AsyncRuntime.evaluate(step)
  def step: Step[A]
  def map[B](f: A => B)(implicit ac: AsyncContext): AsyncFunc0[B] = Map(this, AsyncFunc1.Lifted(f, ac))
  def flatMap[B](f: A => AsyncFunc0[B])(implicit ac: AsyncContext): AsyncFunc0[B] = FlatMap(this, AsyncFunc1.Lifted(f, ac))
  def asyncMap[B]: AsyncFunc1[AsyncFunc1[A, B],AsyncFunc0[B]] = AsyncFunc1.TrivialLifted(f => Map(this, f))
  def asyncFlatMap[B]: AsyncFunc1[AsyncFunc1[A,AsyncFunc0[B]],AsyncFunc0[B]] = AsyncFunc1.TrivialLifted(f => FlatMap(this, f))
  def flatten[B](implicit ev: A <:< AsyncFunc0[B]): AsyncFunc0[B] = Flatten(this.asInstanceOf[AsyncFunc0[AsyncFunc0[B]]] /* FIXME: eliminate cast? */)
}

object AsyncFunc0 {

  def apply[A](f: () => A)(implicit ac: AsyncContext) = Lifted(f, ac)

  final case class Value[+A](a: A) extends AsyncFunc0[A] {
    def step = Result(a)
  }

  final case class Lifted[+A](f: () => A, ac: AsyncContext) extends AsyncFunc0[A] {
    def step = AsyncThunk(() => Result(f()), ac)
  }

  // final case class Step[+A](step0: () => Step[A]) extends AsyncFunc0[A] {
  //   def step = step0
  // }

  final case class Map[A,+B](x: AsyncFunc0[A], y: AsyncFunc1[A,B]) extends AsyncFunc0[B] {
    def step = TrivialThunk(() => x.step.flatMap(a => y.step(a)))
  }

  final case class FlatMap[A,+B](x: AsyncFunc0[A], y: AsyncFunc1[A,AsyncFunc0[B]]) extends AsyncFunc0[B] {
    def step = TrivialThunk(() => x.step.flatMap(a => y.step(a).flatMap(z => z.step)))
  }

  final case class Flatten[+A](x: AsyncFunc0[AsyncFunc0[A]]) extends AsyncFunc0[A] {
    def step = x.step.flatMap(y => y.step)
  }

}