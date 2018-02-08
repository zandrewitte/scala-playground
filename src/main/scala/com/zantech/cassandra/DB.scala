package com.zantech.cassandra

import akka.actor.ActorRef
import com.google.common.util.concurrent.{ FutureCallback, Futures, ListenableFuture }

import scala.concurrent.{ Future, Promise }
import scala.language._
import scalaz.Reader

object DB {

  type Database[T, R] = Reader[T, Future[R]]

  final case class DBSettings[A](session: A, executors: ActorRef)
  final case class Query(q: String)

  final def prepare[T, R](pf: PartialFunction[T, Future[R]]): Database[T, R] =
    Reader(pf)

  final implicit class GuavaFutureOps[A](val f: ListenableFuture[A]) extends AnyVal {

    def asScala: Future[A] = {
      val p = Promise[A]
      Futures.addCallback(
        f,
        new FutureCallback[A] {
          override def onSuccess(a: A)         = p.success(a)
          override def onFailure(t: Throwable) = p.failure(t)
        }
      )
      p.future
    }
  }
}
