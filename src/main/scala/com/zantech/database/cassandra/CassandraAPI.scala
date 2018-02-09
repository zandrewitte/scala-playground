package com.zantech.database.cassandra

import akka.pattern.ask
import akka.util.Timeout
import com.datastax.driver.core._
import com.zantech.database.Database
import com.zantech.database.cassandra.CassandraExecutorActor.{ Prepare, RowOperations }

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scalaz._
import scala.language._
import Database._

object CassandraAPI extends CassandraAPI {

  /**
    * This is used to perform a select query, returns all the records that matches the query and applies
    * the toModel function to all the elements of the list before returning.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    * @return Future list of models.
    */
  override def select[A](boundStatement: Future[A])(implicit executionContext: ExecutionContext,
                                                              timeout: Timeout): DB[List[Row]] =
    prepare {
      case DBSettings(session, actor) =>
        for {
          statement <- boundStatement
          result <- sendExecute(actor, statement, session).mapTo[ResultSet]
        } yield result.all().asScala.toList
    }

  /**
    * This returns a single record from the database for a given query.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    * @return Future optional model.
    */
  override def selectOne[A](boundStatement: Future[A])
                           (implicit executionContext: ExecutionContext, timeout: Timeout): DB[Option[Row]] =
    prepare {
      case DBSettings(session, actor) =>
        for {
          statement <- boundStatement
          result <- sendExecute(actor, statement, session).mapTo[ResultSet]
        } yield Option(result.one())
    }

  /**
    * Executes a statement that does not require a return value.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    * @return Future not used result.
    */
  override def executeAsync[A](boundStatement: Future[A])
                              (implicit executionContext: ExecutionContext, timeout: Timeout): DB[Unit] =
    prepare {
      case DBSettings(session, actor) =>
        for {
          statement <- boundStatement
          _ <- sendExecute(actor, statement, session)
        } yield ()
    }

  /**
    * Used to run the current cassandra values with the specified cassandra settings.
    * @param cassandra current value within cassandra
    * @param cassandraDBSettings the settings to use to run the cassandra reader
    * @tparam A the return type
    * @return future A value
    */
  def runWith[A](cassandra: DB[A], cassandraDBSettings: Settings): Future[A] =
    cassandra run cassandraDBSettings

}

trait CassandraAPI extends Database[Session, Future, Row] {

  final def lift[A](value: A)(implicit executionContext: ExecutionContext): DB[A] =
    prepare { case _ => Future(value) }

  /**
    * Prepares a query string into a prepared statement for the datastax driver.
    *
    * @param query A CQL string query.
    * @return future successfully Prepared statement.
    */
  def prepareAsync(query: Query)(implicit executionContext: ExecutionContext,
                                          timeout: Timeout): DB[PreparedStatement] =
    prepare {
      case DBSettings(session, actor) =>
        (actor ? Prepare(session, query.q)).mapTo[PreparedStatement]
    }

  /**
    * @param preparedStatement The statement that has been prepared by the Cassandra API.
    * @param args Arguments that needs to be bound to the Prepared Statement.
    * @return
    */
  def bind(preparedStatement: Future[PreparedStatement])(args: AnyRef*)(implicit executionContext: ExecutionContext,
                                                                                 timeout: Timeout): DB[BoundStatement] =
    prepare { case _ => preparedStatement.map(_.bind(args)) }

  /**
    *
    * @param ev function that converts a Row to the A type
    * @tparam A The return type
    * @return returns all the rows that were returned as a list of A's.
    */
  def convertTo[F[_], A: ClassTag](futRows: Future[F[Row]])(implicit ev: Row => A,
                                                                     executionContext: ExecutionContext,
                                                                     timeout: Timeout,
                                                                     functor: Functor[F],
                                                                     classTag: ClassTag[F[A]]): DB[F[A]] =
    prepare {
      case DBSettings(_, actor) =>
        for {
          row <- futRows
          result <- (actor ? RowOperations(row, ev)).mapTo[F[A]]
        } yield result
    }
}
