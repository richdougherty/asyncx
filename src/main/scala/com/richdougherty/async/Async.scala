package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * A reified asynchronous computation. An `Async[A]` describes an asynchronous
 * computation that yields an `A` value. An Async value can be executed multiple times by calling
 * its `evaluate()` method. Each call to `evaluate()` returns a `Future[A]`.
 *
 * Constructing an `Async` and then evaluating after construction it to produce a `Future`
 * means that the structure of the computation can be analyzed and the execution
 * optimized.
 */
sealed trait Async[+A] {
  def evaluate(): Future[A] = AsyncRuntime.evaluate(this)
  def map[B](f: A => B)(implicit ac: AsyncContext): Async[B]
  def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B]
  def forEach(f: A => Unit)(implicit ac: AsyncContext): Async[Unit] = ???
  def filter(f: A => Boolean)(implicit ac: AsyncContext): Async[A] = ???
  def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],Async[B]]
  def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,Async[B]],Async[B]]
  def asyncForEach: AsyncFunc[AsyncFunc[A,Boolean],A] = ???
  def asyncFilter: AsyncFunc[AsyncFunc[A,Unit],Unit] = ???
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
    thisThunk =>
    def map[B](f: A => B)(implicit ac: AsyncContext): Async[B] = Thunk(() => x().map(f), TrivialAsyncContext)
    def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B] = Thunk(() => x().flatMap(f), TrivialAsyncContext)
    def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],Async[B]] = Thunk.AsyncMap(this)
    def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,Async[B]],Async[B]] = Thunk.AsyncFlatMap(this)
    def flatten[B](implicit ev: A <:< Async[B]): Async[B] = flatMap(ev)(TrivialAsyncContext)
  }
  object Thunk {
    final case class AsyncMap[A,B](thunk: Thunk[A]) extends AsyncFunc[AsyncFunc[A,B],Async[B]] {
      def apply(f: AsyncFunc[A,B]): Async[Async[B]] = {
        import Implicits.trivial
        for {
          a <- thunk
          b <- f(a)
        } yield Success(b)
      }
    }
    final case class AsyncFlatMap[A,B](thunk: Thunk[A]) extends AsyncFunc[AsyncFunc[A,Async[B]],Async[B]] {
      def apply(f: AsyncFunc[A,Async[B]]): Async[Async[B]] = {
        import Implicits.trivial
        for {
          a <- thunk
          asyncB <- f(a)
        } yield asyncB
      }
    }
  }
}