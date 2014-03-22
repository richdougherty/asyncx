package com.richdougherty.iter

import scala.annotation.tailrec
import scala.collection.{Iterator => ScalaIterator}

/** An immutable iterator */
sealed trait Iterator[+A] {
  def step: Step[A]
  def map[B](f: A => B): Iterator[B]
  def flatMap[B](f: A => Iterator[B]): Iterator[B]
  def fold[B](x: B)(f: (B,A) => B): B = {
    // Implement here, rather than in subclasses, so we can use @tailrec
    @tailrec
    def fold0(iter: Iterator[A], x0: B): B = iter match {
      case Iterator.Element(value, next) => fold0(next(), f(x0, value))
      case Iterator.End => x0
    }
    fold0(this, x)
  }
  def ++[A1>:A](that: Iterator[A1]): Iterator[A1]
  def transform[B](tr: Transformer[A,B]): Iterator[B] = {
    // Implement here, rather than in subclasses, so we can use @tailrec
    @tailrec
    def transform0(iter0: Iterator[A], tr0: Transformer[A,B]): Iterator[B] = (iter0, tr0) match {
      case (_, Transformer.Emitting(value, next)) => Iterator.Element(value, () => transform(tr0))
      case (_, Transformer.Done) => Iterator.End // Or should we throw an exception because an item can't be transformed?
      case (Iterator.Element(value, next), Transformer.Accepting(accept)) => transform0(next(), accept(Step.Element(value)))
      case (Iterator.End, _) => Iterator.End
    }
    transform0(this, tr)
  }
}

object Iterator {

  def fromScalaIterator[A](it: ScalaIterator[A]): Iterator[A] = {
    if (it.hasNext) Element(it.next, () => fromScalaIterator(it)) else End
  }

  final case class Element[+A](value: A, next: () => Iterator[A]) extends Iterator[A] {
    def step = Step.Element(value)
    def map[B](f: A => B): Iterator[B] = Element(f(value), () => next().map(f))
    def flatMap[B](f: A => Iterator[B]): Iterator[B] = f(value)
    def ++[A1>:A](that: Iterator[A1]): Iterator[A1] = {
      Element(value, () => (next() ++ that))
    }
  }

  final case object End extends Iterator[Nothing] {
    def step = Step.End
    def map[B](f: Nothing => B): Iterator[B] = End
    def flatMap[B](f: Nothing => Iterator[B]): Iterator[B] = End
    override def fold[B](x: B)(f: (B,Nothing) => B): B = x
    def ++[A1>:Nothing](that: Iterator[A1]): Iterator[A1] = that
  }

}