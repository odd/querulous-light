package com.twitter.querulous
package database

import java.sql.{Connection, SQLException}
import java.util.concurrent.{TimeoutException => JTimeoutException, _}
import com.twitter.xrayspecs.Duration
// import net.lag.logging.Logger


class SqlDatabaseTimeoutException(msg: String) extends SQLException(msg)

class TimingOutDatabaseFactory(databaseFactory: DatabaseFactory, poolSize: Int, queueSize: Int, openTimeout: Duration, initialTimeout: Duration, maxConnections: Int) extends DatabaseFactory {
  def apply(jdbcDriver: String, jdbcUrl: String, username: String, password: String) = {
    new TimingOutDatabase(databaseFactory(jdbcDriver, jdbcUrl, username, password), jdbcDriver, jdbcUrl, poolSize, queueSize, openTimeout, initialTimeout, maxConnections)
  }
}

class TimingOutDatabase(database: Database, jdbcDriver: String, jdbcUrl: String, poolSize: Int, queueSize: Int, openTimeout: Duration, initialTimeout: Duration, maxConnections: Int) extends Database {
  private val timeout = new FutureTimeout(poolSize, queueSize)
  // private val log = Logger.get(getClass.getName)

  // FIXME not working yet.
  //greedilyInstantiateConnections()

  private def getConnection(wait: Duration) = {
    try {
      timeout(wait) {
        database.open()
      } { conn =>
        database.close(conn)
      }
    } catch {
      case e: TimeoutException =>
        throw new SqlDatabaseTimeoutException(jdbcUrl)
    }
  }

  private def greedilyInstantiateConnections() = {
    // log.info("Connecting to %s", jdbcUrl)
    (0 until maxConnections).map { i =>
      getConnection(initialTimeout)
    }.map(_.close)
  }

  override def open() = getConnection(openTimeout)

  def close(connection: Connection) { database.close(connection) }
}
