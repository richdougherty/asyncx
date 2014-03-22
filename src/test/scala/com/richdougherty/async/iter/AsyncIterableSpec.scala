package com.richdougherty.async.iter

import com.richdougherty.async._

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration

class AsyncIterableSpec extends UnitSpec {

  implicit val ac = AsyncContext.combining(ExecutionContext.global)

  val sumIteratee = AsyncIteratee.forFold[Int,Int](0) { case (sum, el) => sum + el }

  "AsyncIterable.fromIterable" when {
    "applied" should {
      "give an AsyncIterable" in {
        val alist = AsyncIterable.fromIterable(List(1, 2, 3))
        val iterationResult = alist.asyncIterate(sumIteratee)
        await(iterationResult).value should be(6)
      }
    }
  }
}