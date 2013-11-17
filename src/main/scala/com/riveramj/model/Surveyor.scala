package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

case class Surveyor(
  _id: ObjectId,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  salt: String,
  companyId: Option[ObjectId] = None
)
  extends MongoDocument[Surveyor] {
  def meta = Surveyor
}

object Surveyor extends MongoDocumentMeta[Surveyor] {
  override def collectionName = "surveyor"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer
}
