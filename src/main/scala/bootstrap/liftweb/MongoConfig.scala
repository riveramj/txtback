package bootstrap.liftweb

import com.mongodb.{Mongo, ServerAddress}
import net.liftweb.util.Props

import net.liftweb._
import mongodb._

object MongoConfig {

  def init = {
    val srvr = new ServerAddress(
      Props.get("mongo.host", "127.0.0.1"),
      Props.getInt("mongo.port", 27017)
    )
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr), "txtbck")
  }
}
