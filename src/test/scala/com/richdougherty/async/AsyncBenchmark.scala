package com.richdougherty.async

import org.scalameter.api._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

object AsyncBenchmark extends PerformanceTest {

  lazy val executor = LocalExecutor/*SeparateJvmsExecutor*/(
    new Executor.Warmer.Default,
    Aggregator.min,
    new Measurer.Default)
  lazy val reporter = new LoggingReporter
  lazy val persistor = Persistor.None

  val acs: Gen[String] = Gen.enumeration("asyncContext")("fjp-combine", "fjp-separate", "immediate-combine")
  val mapCounts: Gen[Int] = Gen.range("mapCount")(0, 100, 50)
  val inputs = Gen.tupled(acs, mapCounts)

  def acForName(name: String): AsyncContext = name match {
    case "fjp-combine" => AsyncContext.combining(ExecutionContext.global)
    case "fjp-separate" => AsyncContext.separate(ExecutionContext.global)
    case "immediate-combine" => TrivialAsyncContext
    case _ => ???
  }

  performance of "Future" in {
    measure method "map" in {
      using(inputs) in { case (acName, desiredMapCount) =>
        implicit val implicitAC = acForName(acName)
        var fut: Future[Int] = Future(0)
        var mapCount = 0
        while (mapCount < desiredMapCount) {
          fut = fut.map(_ + 1)
          mapCount += 1
        }
        Await.result(fut, Duration.Inf)
      }
    }
  }

  performance of "Async" in {
    measure method "map" in {
      using(inputs) in { case (acName, desiredMapCount) =>
        implicit val implicitAC = acForName(acName)
        var async: Async[Int] = Async(0)
        var mapCount = 0
        while (mapCount < desiredMapCount) {
          async = async.map(_ + 1)
          mapCount += 1
        }
        Await.result(async.evaluate(), Duration.Inf)
      }
    }
  }

  performance of "Future" in {
    measure method "flatMap" in {
      using(inputs) in { case (acName, desiredMapCount) =>
        implicit val implicitAC = acForName(acName)
        var fut: Future[Int] = Future(0)
        var mapCount = 0
        while (mapCount < desiredMapCount) {
          fut = fut.flatMap(x => Future(x + 1))
          mapCount += 1
        }
        Await.result(fut, Duration.Inf)
      }
    }
  }

  performance of "Async" in {
    measure method "flatMap" in {
      using(inputs) in { case (acName, desiredMapCount) =>
        implicit val implicitAC = acForName(acName)
        var async: Async[Int] = Async(0)
        var mapCount = 0
        while (mapCount < desiredMapCount) {
          async = async.flatMap(x => Async(x + 1))
          mapCount += 1
        }
        Await.result(async.evaluate(), Duration.Inf)
      }
    }
  }

}