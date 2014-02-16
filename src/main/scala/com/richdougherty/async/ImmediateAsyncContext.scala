package com.richdougherty.async

import scala.concurrent.ExecutionContext

object ImmediateAsyncContext extends AsyncContext {
  def execute(r: Runnable) = r.run()
  def reportFailure(t: Throwable) = t.printStackTrace()
  def canCombineExecutions(that: AsyncContext) = (this == that)
}