package com.richdougherty.iter

import scala.annotation.tailrec

sealed trait Iteratee[-A,+B] {
  def map[C](f: B => C): Iteratee[A,C]
  // def flatMap[B](f: A => Iteratee[B]): Iteratee[B]
  // def fold[B](x: B)(f: (B,A) => B): [B]
  // def ++[A1>:A](that: Iteratee[A1]): Iteratee[A1]
  def transformInput[C](tr: Transformer[C,A]): Iteratee[C,B]
}

object Iteratee {

  def forFold[A,B](x: B)(f: (B,A) => B): Iteratee[A,B] = {
    def acceptFor(x1: B): Step[A] => Iteratee[A,B] = {
      case Step.Element(value) => Accepting(acceptFor(f(x1,value)))
      case Step.End => Done(x1)
    }
    Accepting(acceptFor(x))
  }

  final case class Accepting[-A,+B](accept: Step[A] => Iteratee[A,B]) extends Iteratee[A,B] {
    def map[C](f: B => C): Iteratee[A,C] = {
      val accept2 = (step: Step[A]) => accept(step).map(f)
      Accepting(accept2)
    }
    def transformInput[C](tr: Transformer[C,A]): Iteratee[C,B] = {
      tr match {
        case Transformer.Accepting(accept) =>
          Iteratee.Accepting((step: Step[C]) => transformInput(accept(step)))
        case Transformer.Emitting(value, next) =>
          accept(Step.Element(value)).transformInput(next())
        case Transformer.Done =>
          accept(Step.End).transformInput(tr /* Transformer.Done */)
      }
    }
  }

  final case class Done[+B](value: B) extends Iteratee[Any,B] {
    def map[C](f: B => C): Iteratee[Any,C] = Done(f(value))
    def transformInput[C](tr: Transformer[C,Any]): Iteratee[C,B] = Done(value)
  }

}