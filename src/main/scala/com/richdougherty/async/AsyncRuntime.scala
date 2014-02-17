package com.richdougherty.async

import scala.annotation.tailrec
import scala.concurrent.{ Future, Promise }
import scala.util.control.NonFatal

import Async._

object AsyncRuntime {
  def evaluate[A](async0: Async[A]): Future[A] = {
    val p = Promise[A]

    def startLoop(async: Async[A], ac: AsyncContext) = try {
      loop(async, ac)
    } catch {
      case NonFatal(t) => Failure(t)
    }

    @tailrec
    def loop(async: Async[A], ac: AsyncContext): Unit = {
      async match {
        case Success(a) =>
          p.success(a)
        case Thunk(f, ac1) if ac.canExecuteWithin(ac) =>
          loop(f(), ac)
        case Thunk(f, ac1) =>
          switchContext(f, ac1)
      }
    }

    def switchContext(f: () => Async[A], ac: AsyncContext): Unit = {
      ac.execute(new Runnable {
        def run() = try {
          val next = f()
          loop(next, ac)
        } catch {
          case NonFatal(t) => Failure(t)
        }
      })
    }

    startLoop(async0, TrivialAsyncContext)
    p.future
  }
}