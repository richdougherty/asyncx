package com.richdougherty.async

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

class AsyncFunc1Spec extends UnitSpec {

  val inc = (x: Int) => x + 1
  val dbl = (x: Int) => x * 2

  implicit val ac = AsyncContext.combining(ExecutionContext.global)

  "AsyncFunc1.Lifted" when {
    "applied" should {
      "return a value" in {
        val f = AsyncFunc1.Lifted(inc, ac)
        await(f(1)) should be (2)
      }
    }
    "mapped" should {
      "return the mapped value" in {
        val f = AsyncFunc1.Lifted(inc, ac)
        await(f.map(dbl).apply(1)) should be (4)
      }
    }
    "flatMapped" should {
      "return the flatMapped value" in {
        val f = AsyncFunc1.Lifted(inc, ac)
        await(f.flatMap(x => f).apply(1)) should be (3)
      }
    }
  }
}