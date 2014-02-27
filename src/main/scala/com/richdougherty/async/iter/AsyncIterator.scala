package com.richdougherty.async.iter

import com.richdougherty.async._

sealed trait AsyncIterator[+A] {
  def step: Step[A]
  def map[B](f: A => B)(implicit ac: AsyncContext): AsyncIterator[B]
  def flatMap[B](f: A => AsyncIterator[B])(implicit ac: AsyncContext): AsyncIterator[B]
  def fold[B](x: B)(f: (B,A) => B)(implicit ac: AsyncContext): Async[B]
  def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],AsyncIterator[B]]
  def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,AsyncIterator[B]],AsyncIterator[B]]
  def asyncFold[B](x: B): AsyncFunc[AsyncFunc[(B,A),B],B]
  def ++[A1>:A](that: AsyncIterator[A1]): AsyncIterator[A1]
}

object AsyncIterator {

  def fromIterator[A](it: Iterator[A])(implicit ac: AsyncContext): Async[AsyncIterator[A]] = {
    Async(if (it.hasNext) Element(it.next, fromIterator(it)) else End)
  }

  final case class Element[+A](value: A, next: Async[AsyncIterator[A]]) extends AsyncIterator[A] {
    def step = Step.Element(value)
    def map[B](f: A => B)(implicit ac: AsyncContext): AsyncIterator[B] = Element(f(value), next.map(_.map(f)))
    def flatMap[B](f: A => AsyncIterator[B])(implicit ac: AsyncContext): AsyncIterator[B] = f(value)
    def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],AsyncIterator[B]] = Element.AsyncMap(this)
    def asyncFlatMap[B]: AsyncFunc[AsyncFunc[A,AsyncIterator[B]],AsyncIterator[B]] = Element.AsyncFlatMap(this)
    def fold[B](x: B)(f: (B,A) => B)(implicit ac: AsyncContext): Async[B] = {
      asyncFold(x)(AsyncFunc((args: (B,A)) => f(args._1, args._2)))
    }
    def asyncFold[B](x: B): AsyncFunc[AsyncFunc[(B,A),B],B] = Element.AsyncFold(this, x)
    def ++[A1>:A](that: AsyncIterator[A1]): AsyncIterator[A1] = {
      import Implicits.trivial
      Element(value, next.map(_ ++ that))
    }
  }

  final case object End extends AsyncIterator[Nothing] {
    def step = Step.End
    def map[B](f: Nothing => B)(implicit ac: AsyncContext): AsyncIterator[B] = End
    def flatMap[B](f: Nothing => AsyncIterator[B])(implicit ac: AsyncContext): AsyncIterator[B] = End
    def asyncMap[B]: AsyncFunc[AsyncFunc[Nothing,B],AsyncIterator[B]] = endFunc
    def asyncFlatMap[B]: AsyncFunc[AsyncFunc[Nothing,AsyncIterator[B]],AsyncIterator[B]] = endFunc
    def fold[B](x: B)(f: (B,Nothing) => B)(implicit ac: AsyncContext): Async[B] = Async.Success(x)
    def asyncFold[B](x: B): AsyncFunc[AsyncFunc[(B,Nothing),B],B] = AsyncFunc.direct(_ => Async.Success(x))
    def ++[A1>:Nothing](that: AsyncIterator[A1]): AsyncIterator[A1] = that
    private val endFunc: AsyncFunc[Any,AsyncIterator[Nothing]] = AsyncFunc.direct(_ => Async.Success(End))
  }

  object Element {
    private final case class AsyncMap[A,B](element: Element[A]) extends AsyncFunc[AsyncFunc[A,B],AsyncIterator[B]] {
      def apply(f: AsyncFunc[A,B]): Async[AsyncIterator[B]] = {
        import Implicits.trivial
        f(element.value).map { element1 =>
          val next1 = element.next.flatMap(it => it.asyncMap(f))
          Element(element1, next1)
        }
      }
    }
    private final case class AsyncFlatMap[A,B](element: Element[A]) extends AsyncFunc[AsyncFunc[A,AsyncIterator[B]],AsyncIterator[B]] {
      def apply(f: AsyncFunc[A,AsyncIterator[B]]): Async[AsyncIterator[B]] = {
        f(element.value)
      }
    }
    private final case class AsyncFold[A,B](element: Element[A], x: B) extends AsyncFunc[AsyncFunc[(B,A),B],B] {
      def apply(f: AsyncFunc[(B,A),B]): Async[B] = {
        import Implicits.trivial
        for {
          x1 <- f((x, element.value))
          it1 <- element.next
          fold1 <- it1.asyncFold(x1)(f)
        } yield fold1
      }
    }
  }

}