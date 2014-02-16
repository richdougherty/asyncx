package com.richdougherty.async

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

class AsyncFunc0Spec extends UnitSpec {

  val one = () => 1

  implicit val ac = AsyncContext.combining(ExecutionContext.global)

  "AsyncFunc0.Lifted" when {
    "applied" should {
      "return a value" in {
        val f = AsyncFunc0.Lifted(one, ac)
        await(f.apply()) should be (1)
      }
    }
    "mapped" should {
      "return the mapped value" in {
        val f = AsyncFunc0.Lifted(one, ac)
        await(f.map(x => x+1).apply()) should be (2)
      }
    }
    "flatMapped" should {
      "return the flatMapped value" in {
        val f = AsyncFunc0.Lifted(one, ac)
        await(f.flatMap(x => f).apply()) should be (1)
      }
    }
  }
}