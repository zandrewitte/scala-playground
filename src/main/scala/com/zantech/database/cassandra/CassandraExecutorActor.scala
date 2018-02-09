package com.zantech.database.cassandra

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, Status }
import akka.routing.FromConfig
import akka.pattern.pipe
import com.datastax.driver.core._
import com.zantech.database.cassandra.CassandraExecutorActor.{ Execute, Prepare, RowOperations }
import com.zantech.database.Database._

import scala.util.control.NonFatal
import scala.language.higherKinds
import scalaz._, Scalaz._

/**
  * The actor that is used to execute asynchronous database queries.
  *
  */
class CassandraExecutorActor extends Actor with ActorLogging {

  import context.dispatcher

  override def receive: Receive = {
    case Execute(session: Session, statement: Statement) =>
      session.executeAsync(statement).asScala.pipeTo(sender())

    case Prepare(session: Session, query) =>
      session.prepareAsync(query).asScala.pipeTo(sender())

    case rowOperations: RowOperations[_, _] =>
      val thatSender = sender()

      try {
        thatSender ! rowOperations.fOperation(rowOperations.rows)(rowOperations.f)
      } catch {
        case NonFatal(e) =>
          log.error(e, "An error occurred when trying to operate on a result set")
          thatSender ! Status.Failure(e)
      }
  }
}

object CassandraExecutorActor {

  def props(system: ActorSystem): Props =
    FromConfig.props(Props[CassandraExecutorActor])

  /**
    * Example Config:
      /parent/CassandraExecutorActor {
        router = round-robin-pool
        resizer {
          lower-bound = 5
          upper-bound = 15
        }
      }
    * @param system Actor System to create the actor on
    * @return Props for CassandraExecutorActor
    */
  def fromConfig(system: ActorSystem): Props =
    FromConfig.props(Props[CassandraExecutorActor])

  /**
    * The case class that is used to send the statement to the Executor Actor.
    *
    * @param statement The prepared statement that needs to be executed.
    */
  final case class Execute[A, B](session: A, statement: B)

  /**
    * The case class that is used to send a string cql statement to the Executor Actor to be prepared for later usage.
    *
    * @param query The CQL query string
    */
  final case class Prepare[A](session: A, query: String)

  /**
    * The trait and following object that is used to operate on a result set, returning a type of T.
    */
  trait RowOperations[F[_], T] {
    def rows: F[Row]
    def operation: Row => T
    def f: Functor[F]
    final def fOperation(rows: F[Row])(implicit F: Functor[F]): F[T] =
      F.map(rows)(operation)
  }

  object RowOperations {
    def apply[F[_], T](values: F[Row], op: Row => T)(implicit F: Functor[F]): RowOperations[F, T] = new RowOperations[F, T] {
      override def rows: F[Row]        = values
      override def operation: Row => T = op
      implicit val f: Functor[F]       = F
    }

  }

}
