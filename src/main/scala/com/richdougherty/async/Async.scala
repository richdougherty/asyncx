package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * A reified asynchronous computation.
 */
sealed trait Async[+A] {
  def evaluate(): Future[A] = AsyncRuntime.evaluate(this)
  def map[B](f: A => B)(implicit ac: AsyncContext): Async[B]
  def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B]
  def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],Async[B]]
  def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,Async[B]],Async[B]]
  def flatten[B](implicit ev: A <:< Async[B]): Async[B]
}

object Async {

  def apply[A](body: => A)(implicit ac: AsyncContext): Thunk[A] = Thunk(() => Success(body), ac)

  final case class Success[+A](a: A) extends Async[A] {
    def map[B](f: A => B)(implicit ac: AsyncContext): Async[B] = Thunk(() => Success(f(a)), ac)
    def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B] = Thunk(() => f(a), ac)
    def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],Async[B]] = AsyncFunc((f: AsyncFunc[A,B]) => f(a))(TrivialAsyncContext)
    def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,Async[B]],Async[B]] = AsyncFunc((f: AsyncFunc[A,Async[B]]) => f(a).flatten)(TrivialAsyncContext)
    def flatten[B](implicit ev: A <:< Async[B]): Async[B] = ev(a)
  }

  final case class Failure(t: Throwable) extends Async[Nothing] {
    def map[A](f: Nothing => A)(implicit ac: AsyncContext): Async[A] = this
    def flatMap[A](f: Nothing => Async[A])(implicit ac: AsyncContext): Async[A] = this
    def asyncMap[A]: AsyncFunc[AsyncFunc[Nothing,A],Async[A]] = AsyncFunc((_: AsyncFunc[Nothing,A]) => this)(TrivialAsyncContext)
    def asyncFlatMap[A]: AsyncFunc[AsyncFunc[Nothing,Async[A]],Async[A]] = AsyncFunc((_: AsyncFunc[Nothing,Async[A]]) => this)(TrivialAsyncContext)
    def flatten[A](implicit ev: Nothing <:< Async[A]): Async[A] = this
  }

  final case class Thunk[+A](x: () => Async[A], ac: AsyncContext) extends Async[A] {
    def map[B](f: A => B)(implicit ac: AsyncContext): Async[B] = Thunk(() => x().map(f), TrivialAsyncContext)
    def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B] = Thunk(() => x().flatMap(f), TrivialAsyncContext)
    def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],Async[B]] = AsyncFunc { (f: AsyncFunc[A,B]) =>
      Thunk(() => x().asyncMap(f).flatten, TrivialAsyncContext)
    }(TrivialAsyncContext)
    def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,Async[B]],Async[B]] = AsyncFunc { (f: AsyncFunc[A,Async[B]]) =>
      Thunk(() => x().asyncMap(f).flatten.flatten, TrivialAsyncContext)
    }(TrivialAsyncContext)
    def flatten[B](implicit ev: A <:< Async[B]): Async[B] = flatMap(ev)(TrivialAsyncContext)
  }

}