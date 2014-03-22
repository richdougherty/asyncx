package com.richdougherty.iter

sealed trait Transformer[-A,+B] {
  def map[C](f: B => C): Transformer[A,C] = ???
  def flatMap[A1<:A,C](f: B => Transformer[A1,C]): Transformer[A,C] = ???
  def ++[A1<:A,B1>:B]: Transformer[A1,B1] = ???
}

object Transformer {

  def mapper[A,B](f: A => B) = {
    def accepting: Accepting[A,B] = Accepting[A,B] {
      case Step.Element(value) => Emitting(f(value), () => accepting)
      case Step.End => Transformer.Done
    }
    accepting
  }

  final case class Accepting[-A,+B](accept: Step[A] => Transformer[A,B]) extends Transformer[A,B]
  final case class Emitting[-A,+B](value: B, next: () => Transformer[A,B]) extends Transformer[A,B]
  final case object Done extends Transformer[Any,Nothing]
}