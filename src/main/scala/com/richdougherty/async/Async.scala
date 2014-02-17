package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * A reified asynchronous computation.
 */
sealed trait Async[+A] {
  //import Async._
  def evaluate(): Future[A] = AsyncRuntime.evaluate(this)
  def map[B](f: A => B)(implicit ac: AsyncContext): Async[B]
  def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B]
  def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],Async[B]]
  def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,Async[B]],Async[B]]// = AsyncFunc.TrivialLifted(f => FlatMap(this, f))
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

// final case class Delayed[+A](fut: Future[A], t: FutureThunk[A]) extends Async[A] {
//   def map[B](f: A => B)(implicit ac: AsyncContext): Async[B] = DelayedThunk(fut, )
//   def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B] = Delayed(fut.flatMap(f))
// }

// object Delayed {
//   val identity = FutureThunk(x: Try)
//   final case class FutureThunk[-A,+B](x: Try[A] => Async[B], ac: ExecutionContext) {
//     def map[C](f: B => C)(implicit ac: AsyncContext): Async[C] = FutureThunk()
//     def flatMap[B](f: A => Async[B])(implicit ac: AsyncContext): Async[B] = Delayed(fut.flatMap(f))
//   }
// }

// object DelayedThunk

// final case class TrivialThunk[+A](x: () => Async[A]) extends Async[A] {
//   def map[B](f: A => B): Async[B] = TrivialThunk(() => x().map(f))
//   def flatMap[B](f: A => Async[B]): Async[B] = x().flatMap(f)
// }

//   def apply[A](f: () => A)(implicit ac: AsyncContext) = Lifted(f, ac)

//   final case class Value[+A](a: A) extends Async[A] {
//     def step = Result(a)
//   }

//   final case class AsyncThunk[+A](f: () => A, ac: AsyncContext) extends Async[A] {
//     def step = AsyncThunk(() => Result(f()), ac)
//   }

//   // final case class Async[+A](step0: () => Async[A]) extends Async[A] {
//   //   def step = step0
//   // }

//   final case class Map[A,+B](x: Async[A], y: AsyncFunc[A,B]) extends Async[B] {
//     def step = TrivialThunk(() => x.step.flatMap(a => y.step(a)))
//   }

//   final case class FlatMap[A,+B](x: Async[A], y: AsyncFunc[A,Async[B]]) extends Async[B] {
//     def step = TrivialThunk(() => x.step.flatMap(a => y.step(a).flatMap(z => z.step)))
//   }

//   final case class Flatten[+A](x: Async[Async[A]]) extends Async[A] {
//     def step = x.step.flatMap(y => y.step)
//   }

// }