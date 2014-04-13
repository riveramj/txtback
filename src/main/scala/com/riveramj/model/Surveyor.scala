package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._
import java.util.Date

case class Surveyor(
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  salt: String,
  phoneNumbers: List[String] = Nil,
  twilioAccountSid: String,
  active: Boolean = false,
  activationKey: Option[String], 
  activationKeyDate: Option[Date]
)
  extends MongoDocument[Surveyor] {
  def meta = Surveyor
}

object Surveyor extends MongoDocumentMeta[Surveyor] {
  override def collectionName = "surveyor"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer
}
