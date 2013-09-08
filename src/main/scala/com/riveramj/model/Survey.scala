package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb.{ObjectIdSerializer, MongoDocumentMeta, MongoDocument}

sealed trait QuestionType
object QuestionType {
  case object trueFalse extends QuestionType
  case object ratingScale extends QuestionType
  case object freeResponse extends QuestionType
  case object choseOne extends QuestionType
}

case class Questions(
  _id: ObjectId,
  question: String,
  questionType: QuestionType,
  questionNumber: Int,
  answers: List[Answers]
)

case class Answers(
  _id: ObjectId,
  answerNumber: Int,
  answer: String
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
