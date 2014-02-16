package com.richdougherty.async

import org.scalameter.api._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

object AsyncBenchmark extends PerformanceTest {

  lazy val executor = SeparateJvmsExecutor(
    new Executor.Warmer.Default,
    Aggregator.min,
    new Measurer.Default)
  lazy val reporter = new LoggingReporter
  lazy val persistor = Persistor.None

  val ecs: Gen[String] = Gen.enumeration("executionContext")("fjp", "imm")
  val mapCounts: Gen[Int] = Gen.range("mapCount")(0, 500, 100)
  val inputs = Gen.tupled(ecs, mapCounts)

  def ecForName(name: String): ExecutionContext = name match {
    case "fjp" => ExecutionContext.global
    case "imm" => ImmediateAsyncContext
    case _ => ???
  }

  performance of "Future" in {
    measure method "map" in {
      using(inputs) in { case (ecName, desiredMapCount) =>
        implicit val implicitEC = ecForName(ecName)
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

  performance of "AsyncFunc0" in {
    measure method "map" in {
      using(inputs) in { case (ecName, desiredMapCount) =>
        implicit val ac: AsyncContext = AsyncContext.combining(ecForName(ecName))
        var func: AsyncFunc0[Int] = AsyncFunc0.Lifted(() => 0, ac)
        var mapCount = 0
        while (mapCount < desiredMapCount) {
          func = func.map(_ + 1)
          mapCount += 1
        }
        Await.result(func.apply(), Duration.Inf)
      }
    }
  }

  performance of "Future" in {
    measure method "flatMap" in {
      using(inputs) in { case (ecName, desiredMapCount) =>
        implicit val implicitEC = ecForName(ecName)
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

  performance of "AsyncFunc0" in {
    measure method "flatMap" in {
      using(inputs) in { case (ecName, desiredMapCount) =>
        implicit val ac: AsyncContext = AsyncContext.combining(ecForName(ecName))
        var func: AsyncFunc0[Int] = AsyncFunc0.Lifted(() => 0, ac)
        var mapCount = 0
        while (mapCount < desiredMapCount) {
          func = func.flatMap(x => AsyncFunc0.Lifted(() => x + 1, ac))
          mapCount += 1
        }
        Await.result(func.apply(), Duration.Inf)
      }
    }
  }

}