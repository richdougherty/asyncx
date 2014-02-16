package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.{ Future, Promise }

object AsyncRuntime {
  def evaluate[A](step0: Step[A]): Future[A] = {
    val p = Promise[A]
    @tailrec
    def loop(step: Step[A], acOrNull: AsyncContext): Unit = {
      step match {
        case Result(a) =>
          p.success(a)
        case TrivialThunk(f) =>
          loop(f(), acOrNull)
        case AsyncThunk(f, ac) if (acOrNull == ac && (acOrNull canCombineExecutions ac)) =>
          loop(f(), acOrNull)
        case AsyncThunk(f, ac) =>
          ac.execute(new Runnable {
            def run() = reenterLoop(f(), ac)
          })
      }
    }
    def reenterLoop(step: Step[A], acOrNull: AsyncContext): Unit = {
      loop(step, acOrNull)
    }
    loop(step0, null)
    p.future
  }
}

sealed trait Step[+A] {
  def map[B](f: A => B): Step[B]
  def flatMap[B](f: A => Step[B]): Step[B]
}
final case class Result[+A](a: A) extends Step[A] {
  def map[B](f: A => B): Step[B] = Result(f(a))
  def flatMap[B](f: A => Step[B]): Step[B] = f(a)
}
final case class TrivialThunk[+A](x: () => Step[A]) extends Step[A] {
  def map[B](f: A => B): Step[B] = TrivialThunk(() => x().map(f))
  def flatMap[B](f: A => Step[B]): Step[B] = x().flatMap(f)
}
final case class AsyncThunk[+A](x: () => Step[A], ac: AsyncContext) extends Step[A] {
  def map[B](f: A => B): Step[B] = AsyncThunk(() => x().map(f), ac)
  def flatMap[B](f: A => Step[B]): Step[B] = x().flatMap(f)
}
