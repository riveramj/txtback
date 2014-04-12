package bootstrap.liftweb

import com.mongodb.{Mongo, ServerAddress, MongoClient}
import net.liftweb.util.Props

import net.liftweb._
import mongodb._

object MongoConfig {

  def init = {
    val address = new ServerAddress(
      Props.get("mongo.host", "127.0.0.1"),
      Props.getInt("mongo.port", 27017)
    )
    
    val mongo = new MongoClient(address)
    
    MongoDB.defineDb(DefaultMongoIdentifier, mongo, "txtback")
  }
}
