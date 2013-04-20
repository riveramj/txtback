package com.riveramj.db

import net.liftweb.util.Props
import java.sql.Connection
import net.liftweb.common.{Full, Box}
import net.liftweb.db.{ConnectionIdentifier, ConnectionManager}
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.beans.PropertyVetoException
import javax.sql.DataSource

object C3P0DBVendor extends ConnectionManager {

  private val dataSource = buildDataSource

  private lazy val chooseDriver = {
    Props.get("db.driver") openOr "org.h2.Driver"
  }

  private lazy val chooseURL = {
    Props.get("db.url") openOr "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE"
  }

  def newConnection(name: ConnectionIdentifier): Box[Connection] = {
    Full(dataSource.getConnection)
  }

  def releaseConnection(conn: Connection): Unit = {
    conn.close();
  }

  def shutDown(): Unit = {
    dataSource.asInstanceOf[ComboPooledDataSource].close()
  }

  def getDataSource(): DataSource = {
    dataSource
  }

  private def buildDataSource: DataSource = {
    try {
      val dataSource = new ComboPooledDataSource()
      dataSource.setDriverClass(chooseDriver)
      dataSource.setJdbcUrl(chooseURL)
      dataSource.setAutomaticTestTable("CM_CONNECTION_TEST")
      dataSource.setIdleConnectionTestPeriod(600)

      val credentials = (Props.get("db.user"), Props.get("db.password"))
      credentials match {
        case (Full(user), Full(password)) => {
          dataSource.setUser(user)
          dataSource.setPassword(password)
        }
        case _ => ()
      }

      dataSource
    }
    catch {
      case ex: PropertyVetoException => throw new RuntimeException("Could not initialize database connection", ex)
    }

  }


}