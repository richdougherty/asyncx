package com.richdougherty.async

import scala.concurrent.ExecutionContext

trait AsyncContext extends ExecutionContext {
  final override def prepare = this
  def canCombineExecutions(that: AsyncContext): Boolean
}

object AsyncContext {
  implicit def combining(ec: ExecutionContext) = new AsyncContext {
    val prepared = ec.prepare
    def execute(r: Runnable) = prepared.execute(r)
    def canCombineExecutions(that: AsyncContext) = (this == that)
    def reportFailure(t: Throwable) = prepared.reportFailure(t)
  }
}