package com.richdougherty.async.iter

import com.richdougherty.async._

sealed trait AsyncIteratee[-A,+B] {
  def map[C](f: B => C)(implicit ac: AsyncContext): AsyncIteratee[A,C]
  // def map[B](f: A => B)(implicit ac: AsyncContext): AsyncIteratee[B]
  // def flatMap[B](f: A => AsyncIteratee[B])(implicit ac: AsyncContext): AsyncIteratee[B]
  // def fold[B](x: B)(f: (B,A) => B)(implicit ac: AsyncContext): Async[B]
  def asyncMap[C]: AsyncFunc[AsyncFunc[B,C],AsyncIteratee[A,C]]
  // def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,AsyncIteratee[B]],AsyncIteratee[B]]
  // def asyncFold[B](x: B): AsyncFunc[AsyncFunc[(B,A),B],B]
  // def ++[A1>:A](that: AsyncIteratee[A1]): AsyncIteratee[A1]
}

object AsyncIteratee {

  def fromFold[A,B](x: B)(f: (B,A) => B)(implicit ac: AsyncContext): AsyncIteratee[A,B] = {
    def acceptFor(x1: B): AsyncFunc[Step[A],AsyncIteratee[A,B]] = AsyncFunc {
      case Step.Element(value) => Accepting(acceptFor(f(x1,value)))
      case Step.End => Done(x1)
    }
    Accepting(acceptFor(x))
  }

  final case class Accepting[-A,+B](accept: AsyncFunc[Step[A],AsyncIteratee[A,B]]) extends AsyncIteratee[A,B] {
    def map[C](f: B => C)(implicit ac: AsyncContext): AsyncIteratee[A,C] = {
      val accept2 = accept.map((ab: AsyncIteratee[A,B]) => ab.map(f))
      Accepting(accept2)
    }
    def asyncMap[C]: AsyncFunc[AsyncFunc[B,C],AsyncIteratee[A,C]] = Accepting.Map(this)
  }

  final case class Done[+B](value: B) extends AsyncIteratee[Any,B] {
    def map[C](f: B => C)(implicit ac: AsyncContext): AsyncIteratee[Any,C] = Done(f(value))
    def asyncMap[C]: AsyncFunc[AsyncFunc[B,C],AsyncIteratee[Any,C]] = AsyncFunc.direct { (f: AsyncFunc[B,C]) =>
      f(value).map(Done(_))(TrivialAsyncContext)
    }
  }

  object Accepting {
    private final case class Map[A,B,C](ie: Accepting[A,B]) extends AsyncFunc[AsyncFunc[B,C],AsyncIteratee[A,C]] {
      def apply(f: AsyncFunc[B,C]): Async[AsyncIteratee[A,C]] = {
        val accept2 = AsyncFunc.direct { (stepA: Step[A]) =>
          import Implicits.trivial
          ie.accept(stepA).flatMap(ie2 => ie2.asyncMap(f))
        }
        Async.Success(Accepting(accept2))
      }
    }
  }

  // def fromIterator[A](it: Iterator[A])(implicit ac: AsyncContext): Async[AsyncIteratee[A]] = {
  //   Async(if (it.hasNext) Element(it.next, fromIterator(it)) else Done)
  // }

  // final case class Element[+A](value: A, next: Async[AsyncIteratee[A]]) extends AsyncIteratee[A] {
  //   def map[B](f: A => B)(implicit ac: AsyncContext): AsyncIteratee[B] = Element(f(value), next.map(_.map(f)))
  //   def flatMap[B](f: A => AsyncIteratee[B])(implicit ac: AsyncContext): AsyncIteratee[B] = f(value)
  //   def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],AsyncIteratee[B]] = Element.AsyncMap(this)
  //   def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,AsyncIteratee[B]],AsyncIteratee[B]] = Element.AsyncFlatMap(this)
  //   def fold[B](x: B)(f: (B,A) => B)(implicit ac: AsyncContext): Async[B] = {
  //     asyncFold(x)(AsyncFunc((args: (B,A)) => f(args._1, args._2)))
  //   }
  //   def asyncFold[B](x: B): AsyncFunc[AsyncFunc[(B,A),B],B] = Element.AsyncFold(this, x)
  //   def ++[A1>:A](that: AsyncIteratee[A1]): AsyncIteratee[A1] = {
  //     import Implicits.trivial
  //     Element(value, next.map(_ ++ that))
  //   }
  // }

  // final case object Done extends AsyncIteratee[Nothing] {
  //   def map[B](f: Nothing => B)(implicit ac: AsyncContext): AsyncIteratee[B] = Done
  //   def flatMap[B](f: Nothing => AsyncIteratee[B])(implicit ac: AsyncContext): AsyncIteratee[B] = Done
  //   def asyncMap[B]: AsyncFunc[AsyncFunc[Nothing,B],AsyncIteratee[B]] = doneFunc
  //   def asyncFlatMap[B]: AsyncFunc[AsyncFunc[Nothing,AsyncIteratee[B]],AsyncIteratee[B]] = doneFunc
  //   def fold[B](x: B)(f: (B,Nothing) => B)(implicit ac: AsyncContext): Async[B] = Async.Success(x)
  //   def asyncFold[B](x: B): AsyncFunc[AsyncFunc[(B,Nothing),B],B] = AsyncFunc.direct(_ => Async.Success(x))
  //   def ++[A1>:Nothing](that: AsyncIteratee[A1]): AsyncIteratee[A1] = that
  //   private val doneFunc: AsyncFunc[Any,AsyncIteratee[Nothing]] = AsyncFunc.direct(_ => Async.Success(Done))
  // }

  // object Element {
  //   private final case class AsyncMap[A,B](element: Element[A]) extends AsyncFunc[AsyncFunc[A,B],AsyncIteratee[B]] {
  //     def apply(f: AsyncFunc[A,B]): Async[AsyncIteratee[B]] = {
  //       import Implicits.trivial
  //       f(element.value).map { element1 =>
  //         val next1 = element.next.flatMap(it => it.asyncMap(f))
  //         Element(element1, next1)
  //       }
  //     }
  //   }
  //   private final case class AsyncFlatMap[A,B](element: Element[A]) extends AsyncFunc[AsyncFunc[A,AsyncIteratee[B]],AsyncIteratee[B]] {
  //     def apply(f: AsyncFunc[A,AsyncIteratee[B]]): Async[AsyncIteratee[B]] = {
  //       f(element.value)
  //     }
  //   }
  //   private final case class AsyncFold[A,B](element: Element[A], x: B) extends AsyncFunc[AsyncFunc[(B,A),B],B] {
  //     def apply(f: AsyncFunc[(B,A),B]): Async[B] = {
  //       import Implicits.trivial
  //       for {
  //         x1 <- f((x, element.value))
  //         it1 <- element.next
  //         fold1 <- it1.asyncFold(x1)(f)
  //       } yield fold1
  //     }
  //   }
  // }

}