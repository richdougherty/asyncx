package com.richdougherty.async

sealed trait AsyncIterator[+A] {
  import AsyncIterator._
  def state: AsyncFunc0[State[A]]
  def fold[B]: AsyncFunc1[AsyncFunc1[State[A],B],B] = new AsyncFunc1[AsyncFunc1[State[A],B],B] {
    def step(f: AsyncFunc1[State[A],B]): Step[B] = {
      for {
        s <- state.step
        b <- f.step(s)
      } yield b
    }
  }
  def map[B](f: A => B)(implicit ac: AsyncContext): AsyncIterator[B] = asyncMap(AsyncFunc1(f))
  def flatMap[B](f: A => AsyncIterator[B])(implicit ac: AsyncContext): AsyncIterator[B] = asyncFlatMap(AsyncFunc1(f))
  def asyncMap[B](f: AsyncFunc1[A, B]): AsyncIterator[B]
  def asyncFlatMap[B](f: AsyncFunc1[A, AsyncIterator[B]]): AsyncIterator[B]
}

object AsyncIterator {

  sealed trait State[+A]
  final case class Emitting[+A](
    val element: A,
    val next: AsyncFunc0[AsyncIterator[A]]
  ) extends State[A]
  final case object Done extends State[Nothing]

  // final case class Map[A,+B](x: AsyncIterator[A], f: AsyncFunc1[A, B]) extends AsyncIterator[B] {
  //   //def state: AsyncFunc0[State[B]] = 
  // }

  // final case class FlatMap[A,+B](x: AsyncIterator[A], f: AsyncFunc1[A, AsyncIterator[B]]) extends AsyncIterator[B] {
  // }

}