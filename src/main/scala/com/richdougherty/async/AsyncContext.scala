package com.richdougherty.async

import scala.language.implicitConversions

import scala.concurrent.ExecutionContext

trait AsyncContext extends ExecutionContext {
  final override def prepare = this
  // Not symmetric
  def canExecuteWithin(that: AsyncContext): Boolean
}

object AsyncContext {
  implicit def combining(ec: ExecutionContext) = new AsyncContext {
    val prepared = ec.prepare
    def execute(r: Runnable) = prepared.execute(r)
    def canExecuteWithin(that: AsyncContext) = (this == that)
    def reportFailure(t: Throwable) = prepared.reportFailure(t)
  }
  implicit def separate(ec: ExecutionContext) = new AsyncContext {
    val prepared = ec.prepare
    def execute(r: Runnable) = prepared.execute(r)
    def canExecuteWithin(that: AsyncContext) = false
    def reportFailure(t: Throwable) = prepared.reportFailure(t)
  }
}

object Implicits {
  implicit val trivial = TrivialAsyncContext
}

object TrivialAsyncContext extends AsyncContext {
  def execute(r: Runnable) = r.run()
  def reportFailure(t: Throwable) = t.printStackTrace()
  def canExecuteWithin(that: AsyncContext) = true
}