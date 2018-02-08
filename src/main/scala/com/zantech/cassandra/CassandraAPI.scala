package com.zantech.cassandra

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.datastax.driver.core._
import com.zantech.cassandra.CassandraExecutorActor.{ Execute, Prepare, RowOperations }
import com.zantech.cassandra.DB.{ DBSettings, Database, Query }

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scalaz._
import scala.language._

object CassandraAPI extends CassandraAPI {
  final implicit val cassandraAPI: CassandraAPI = CassandraAPI

  /**
    * Prepares a query string into a prepared statement for the datastax driver.
    *
    * @param query A CQL string query.
    * @return future successfully Prepared statement.
    */
  override def prepareAsync(query: Query)(implicit executionContext: ExecutionContext,
                                          timeout: Timeout): Cassandra[PreparedStatement] =
    DB.prepare {
      case DBSettings(session, actor) =>
        (actor ? Prepare(session, query.q)).mapTo[PreparedStatement]
    }

  /**
    * @param preparedStatement The statement that has been prepared by the Cassandra API.
    * @param args Arguments that needs to be bound to the Prepared Statement.
    * @return
    */
  override def bind(preparedStatement: Future[PreparedStatement])(args: AnyRef*)(implicit executionContext: ExecutionContext,
                                                                                 timeout: Timeout): Cassandra[BoundStatement] =
    DB.prepare { case _ => preparedStatement.map(_.bind(args)) }

  /**
    * This is used to perform a select query, returns all the records that matches the query and applies
    * the toModel function to all the elements of the list before returning.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    * @return Future list of models.
    */
  override def select(boundStatement: Future[BoundStatement])(implicit executionContext: ExecutionContext,
                                                              timeout: Timeout): Cassandra[List[Row]] =
    DB.prepare {
      case DBSettings(session, actor) =>
        for {
          statement <- boundStatement
          result <- sendExecute(actor, statement, session)
        } yield result.all().asScala.toList
    }

  /**
    * This returns a single record from the database for a given query.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    * @return Future optional model.
    */
  override def selectOne(boundStatement: Future[BoundStatement])(implicit executionContext: ExecutionContext,
                                                                 timeout: Timeout): Cassandra[Option[Row]] =
    DB.prepare {
      case DBSettings(session, actor) =>
        for {
          statement <- boundStatement
          result <- sendExecute(actor, statement, session)
        } yield Option(result.one())
    }

  /**
    * Executes a statement that does not require a return value.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    * @return Future not used result.
    */
  override def executeAsync(boundStatement: Future[BoundStatement])(implicit executionContext: ExecutionContext,
                                                                    timeout: Timeout): Cassandra[Unit] =
    DB.prepare {
      case DBSettings(session, actor) =>
        for {
          statement <- boundStatement
          _ <- sendExecute(actor, statement, session)
        } yield Unit
    }

  /**
    *
    * @param ev function that converts a Row to the A type
    * @tparam A The return type
    * @return returns all the rows that were returned as a list of A's.
    */
  override def convertTo[F[_], A: ClassTag](futRows: Future[F[Row]])(implicit ev: Row => A,
                                                                     executionContext: ExecutionContext,
                                                                     timeout: Timeout,
                                                                     functor: Functor[F],
                                                                     classTag: ClassTag[F[A]]): Cassandra[F[A]] =
    DB.prepare {
      case DBSettings(_, actor) =>
        for {
          row <- futRows
          result <- (actor ? RowOperations(row, ev)).mapTo[F[A]]
        } yield result
    }

  /**
    * Used to run the current cassandra values with the specified cassandra settings.
    * @param cassandra current value within cassandra
    * @param cassandraDBSettings the settings to use to run the cassandra reader
    * @tparam A the return type
    * @return future A value
    */
  def runWith[A](cassandra: Cassandra[A], cassandraDBSettings: CassandraDBSettings): Future[A] =
    cassandra run cassandraDBSettings

  /**
    * The generic function that is used to send a statement to be executed asynchronously.
    *
    * @param statement the statement to be executed.
    * @return a ResultSet for the performed statement.
    */
  private def sendExecute(executorActor: ActorRef,
                          statement: BoundStatement,
                          session: Session)(implicit executionContext: ExecutionContext, timeout: Timeout): Future[ResultSet] =
    (executorActor ? Execute(session, statement)).mapTo[ResultSet]

}

trait CassandraAPI {
  final type Cassandra[A]        = Database[CassandraDBSettings, A]
  final type CassandraDBSettings = DBSettings[Session]

  final def lift[A](value: A)(implicit executionContext: ExecutionContext): Cassandra[A] =
    DB.prepare { case _ => Future { value } }

  def prepareAsync(query: Query)(implicit executionContext: ExecutionContext, timeout: Timeout): Cassandra[PreparedStatement]

  def bind(preparedStatement: Future[PreparedStatement])(args: AnyRef*)(implicit executionContext: ExecutionContext,
                                                                        timeout: Timeout): Cassandra[BoundStatement]

  def select(boundStatement: Future[BoundStatement])(implicit executionContext: ExecutionContext,
                                                     timeout: Timeout): Cassandra[List[Row]]

  def selectOne(boundStatement: Future[BoundStatement])(implicit executionContext: ExecutionContext,
                                                        timeout: Timeout): Cassandra[Option[Row]]

  def executeAsync(boundStatement: Future[BoundStatement])(implicit executionContext: ExecutionContext,
                                                           timeout: Timeout): Cassandra[Unit]

  def convertTo[F[_], A: ClassTag](futRows: Future[F[Row]])(implicit ev: Row => A,
                                                            executionContext: ExecutionContext,
                                                            timeout: Timeout,
                                                            functor: Functor[F],
                                                            classTag: ClassTag[F[A]]): Cassandra[F[A]]

}
