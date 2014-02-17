package com.richdougherty.async

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

class AsyncFuncSpec extends UnitSpec {

  val inc = (x: Int) => x + 1
  val dbl = (x: Int) => x * 2

  implicit val ac = AsyncContext.combining(ExecutionContext.global)

  "AsyncFunc.Lifted" when {
    "applied" should {
      "return a value" in {
        val f = AsyncFunc(inc)
        await(f.evaluate(1)) should be (2)
      }
    }
    "mapped" should {
      "return the mapped value" in {
        val f = AsyncFunc(inc)
        await(f.map(dbl).evaluate(1)) should be (4)
      }
    }
    "flatMapped" should {
      "return the flatMapped value" in {
        val f = AsyncFunc.Lifted(inc, ac)
        await(f.flatMap(x => f).evaluate(1)) should be (3)
      }
    }
  }
}