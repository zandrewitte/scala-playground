package com.zantech.database

import akka.pattern.ask
import akka.actor.ActorRef
import akka.util.Timeout
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import com.zantech.database.cassandra.CassandraExecutorActor.Execute

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language._
import scalaz.Reader
import Database._

trait Database[T, W[_], R] {
  type Settings = DBSettings[T]
  type DB[A] = Reader[Settings, W[A]]

  final def prepare[A](pf: PartialFunction[Settings, W[A]]): DB[A] =
    Reader(pf)

  def select[A](statement: W[A])
               (implicit executionContext: ExecutionContext, timeout: Timeout): DB[List[R]]

  def selectOne[A](statement: W[A])(implicit executionContext: ExecutionContext,
                                                        timeout: Timeout): DB[Option[R]]

  def executeAsync[A](statement: W[A])(implicit executionContext: ExecutionContext,
                                                           timeout: Timeout): DB[Unit]

  /**
    * The generic function that is used to send a statement to be executed asynchronously.
    *
    * @param statement the statement to be executed.
    * @return a ResultSet for the performed statement.
    */
  protected def sendExecute[A](executorActor: ActorRef, statement: A, session: T)
                            (implicit executionContext: ExecutionContext, timeout: Timeout): Future[_] =
    executorActor ? Execute(session, statement)
}

object Database {

  final case class DBSettings[A](session: A, executor: ActorRef)
  final case class Query(q: String)

  final implicit class GuavaFutureOps[A](val f: ListenableFuture[A]) extends AnyVal {
    def asScala: Future[A] = {
      val p = Promise[A]
      Futures.addCallback(
        f,
        new FutureCallback[A] {
          override def onSuccess(a: A) = p.success(a)
          override def onFailure(t: Throwable) = p.failure(t)
        }
      )
      p.future
    }
  }
}