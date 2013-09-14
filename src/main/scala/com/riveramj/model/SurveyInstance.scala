package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._
import org.joda.time.DateTime


sealed trait SurveyInstanceStatus
object SurveyInstanceStatus {
  case object Inactive extends SurveyInstanceStatus
  case object Active extends SurveyInstanceStatus
  case object Finished extends SurveyInstanceStatus
}

case class QuestionAnswers(
  questionId: ObjectId,
  answer: String,
  responseDate: DateTime
)

case class SurveyInstance(
  _id: ObjectId,
  surveyId: ObjectId,
  responderPhone: String,
  status: SurveyInstanceStatus,
  nextQuestionId: Option[ObjectId] = None,
  responses: List[QuestionAnswers]
)
  extends MongoDocument[SurveyInstance] {
  def meta = SurveyInstance
}

object SurveyInstance extends MongoDocumentMeta[SurveyInstance] {
  override def collectionName = "surveyInstance"
  override def formats = super.formats + new ObjectIdSerializer + new DateSerializer
}
