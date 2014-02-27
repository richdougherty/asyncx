package com.richdougherty.async

import org.scalatest._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

abstract class UnitSpec extends WordSpec with Matchers with
  OptionValues with Inside with Inspectors {

  def await[A](a: Async[A]): A = await(a.evaluate())
  def await[A](f: Future[A]): A = Await.result(f, Duration.Inf)
  def ready[A](a: Async[A]): Future[A] = ready(a.evaluate())
  def ready[A](f: Future[A]): Future[A] = Await.ready(f, Duration.Inf)

}