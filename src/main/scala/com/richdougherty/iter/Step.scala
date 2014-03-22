package com.richdougherty.iter

sealed trait Step[+A] {
  def map[B](f: A => B): Step[B]
  def flatMap[B](f: A => Step[B]): Step[B]
}

object Step {

  final case class Element[+A](value: A) extends Step[A] {
    def map[B](f: A => B) = Element(f(value))
    def flatMap[B](f: A => Step[B]) = f(value)
  }

  final case object End extends Step[Nothing] {
    def map[B](f: Nothing => B) = End
    def flatMap[B](f: Nothing => Step[B]) = End
  }

}
