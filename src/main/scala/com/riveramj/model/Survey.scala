package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb.{ObjectIdSerializer, MongoDocumentMeta, MongoDocument}

case class Questions(
  _id: ObjectId,
  question: String,
  questionType: String,
  questionNumber: Int
)

case class Survey(
  _id: ObjectId,
  name: String,
  companyId: ObjectId,
  questions: List[Questions]

)
  extends MongoDocument[Survey] {
  def meta = Survey
}

object Survey extends MongoDocumentMeta[Survey] {
  override def collectionName = "survey"
  override def formats = super.formats + new ObjectIdSerializer
}
