package com.zantech.cassandra

import akka.util.Timeout
import com.datastax.driver.core._
import CassandraAPI.{ Cassandra, CassandraDBSettings }
import DB.Query

import scala.concurrent.{ ExecutionContext, Future }
import scala.language._
import scala.reflect.ClassTag
import scalaz.Functor

object Syntax {

  implicit class cassandraOps[A](cassandra: Cassandra[A]) {
    def runWith(cassandraDBSettings: CassandraDBSettings): Future[A] =
      CassandraAPI.runWith(cassandra, cassandraDBSettings)
  }

  implicit class futRowOps[F[_]](futRows: Future[F[Row]]) {

    /**
      *
      * @param ev function that converts a Row to the A type
      * @tparam A The return type
      * @return returns all the rows that were returned as a list of A's.
      */
    def to[A: ClassTag](implicit cassandraAPI: CassandraAPI,
                        ev: Row => A,
                        executionContext: ExecutionContext,
                        timeout: Timeout,
                        functor: Functor[F],
                        classTag: ClassTag[F[A]]): Cassandra[F[A]] =
      cassandraAPI.convertTo(futRows)
  }

  /** The implicit class that binds the Cassandra API functions to all bound statements.
    *
    * @param boundStatement the prepared statement that has been bound with the required arguments.
    */
  implicit class boundStatementOps(boundStatement: Future[BoundStatement]) {

    /**
      * This is used to perform a select query, returns all the records that matches the query and applies
      * the toModel function to all the elements of the list before returning.
      *
      * @return Future list of models.
      */
    def select(implicit cassandraAPI: CassandraAPI, executionContext: ExecutionContext, timeout: Timeout): Cassandra[List[Row]] =
      cassandraAPI.select(boundStatement)

    /**
      * This returns a single record from the database for a given query.
      *
      * @return Future optional model.
      */
    def selectOne(implicit cassandraAPI: CassandraAPI,
                  executionContext: ExecutionContext,
                  timeout: Timeout): Cassandra[Option[Row]] =
      cassandraAPI.selectOne(boundStatement)

    /**
      * Executes a statement that does not require a return value.
      *
      * @return Future not used result.
      */
    def executeAsync(implicit cassandraAPI: CassandraAPI, executionContext: ExecutionContext, timeout: Timeout): Cassandra[Unit] =
      cassandraAPI.executeAsync(boundStatement)
  }

  /** The implicit class that allows the user to bind arguments to prepared statements.
    *
    * @param preparedStatement The statement that has been prepared by the Cassandra API.
    */
  implicit class preparedStatementOps(preparedStatement: Future[PreparedStatement]) {

    /**
      *
      * @param args Arguments that needs to be bound to the Prepared Statement.
      * @return
      */
    def bind(
        args: AnyRef*
    )(implicit cassandraAPI: CassandraAPI, executionContext: ExecutionContext, timeout: Timeout): Cassandra[BoundStatement] =
      cassandraAPI.bind(preparedStatement)(args)
  }

  /** The implicit class that allows the user to convert a query string to be converted to a Cassandra prepared statement.
    *
    * @param query A CQL query.
    */
  implicit class stringOps(query: String) {
    def prepareAsync(implicit cassandraAPI: CassandraAPI,
                     executionContext: ExecutionContext,
                     timeout: Timeout): Cassandra[PreparedStatement] =
      cassandraAPI.prepareAsync(Query(query))
  }

  /** The implicit class that allows the user to convert a query to a Cassandra prepared statement.
    *
    * @param query A CQL query.
    */
  implicit class queryOps(query: Query) {

    /**
      * Prepares a query string into a prepared statement for the datastax driver.
      *
      * @return future successfully Prepared statement.
      */
    def prepareAsync(implicit cassandraAPI: CassandraAPI,
                     executionContext: ExecutionContext,
                     timeout: Timeout): Cassandra[PreparedStatement] =
      cassandraAPI.prepareAsync(query)
  }
}
