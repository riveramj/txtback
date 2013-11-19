package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._

case class Company(
  _id: ObjectId,
  name: String,
  shadow: Boolean = false
)
  extends MongoDocument[Company] {
  def meta = Company
}

object Company extends MongoDocumentMeta[Company] {
  override def collectionName = "company"
  override def formats = super.formats + new ObjectIdSerializer + new PatternSerializer
}
