package com.richdougherty.async

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

class AsyncSpec extends UnitSpec {

  implicit val ac = AsyncContext.combining(ExecutionContext.global)

  "Async.Thunk" when {
    "evaluated" should {
      "return a value" in {
        val f = Async(1)
        await(f.evaluate()) should be (1)
      }
    }
    "mapped" should {
      "return the mapped value" in {
        val f = Async(1)
        await(f.map(x => x+1).evaluate()) should be (2)
      }
    }
    "flatMapped" should {
      "return the flatMapped value" in {
        val f = Async(1)
        await(f.flatMap(x => f).evaluate()) should be (1)
      }
    }
  }
}