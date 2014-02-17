// package com.richdougherty.async

// trait AsyncValue[+A] {
//   import AsyncValue._
//   def get: Async[A]
//   def map[B](f: A => B)(implicit ac: AsyncContext): AsyncValue[B] = Map(this, AsyncFunc.Lifted(f, ac))
//   def flatMap[B](f: A => AsyncValue[B])(implicit ac: AsyncContext): AsyncValue[B] = FlatMap(this, AsyncFunc.Lifted(f, ac))
//   def asyncMap[B]: AsyncFunc[AsyncFunc[A,B],AsyncValue[B]] = AsyncFunc.TrivialLifted(f => Map(this, f))
// }

// object AsyncValue {
//   final case class Map[A,+B](x: AsyncValue[A], f: AsyncFunc[A, B]) extends AsyncValue[B] {
//     def get: Async[B] = x.get.asyncMap.curry(f).flatten
//   }
//   final case class FlatMap[A,+B](x: AsyncValue[A], f: AsyncFunc[A, AsyncValue[B]]) extends AsyncValue[B] {
//     def get: Async[B] = x.get.asyncFlatMap.curry()
//   }
// }