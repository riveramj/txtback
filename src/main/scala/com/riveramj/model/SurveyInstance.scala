package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb._
import org.joda.time.DateTime
import net.liftweb.json.{TypeInfo, Formats, Serializer}
import net.liftweb.json.JsonDSL._

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
  currentQuestionId: Option[ObjectId] = None,
  nextQuestionId: Option[ObjectId] = None,
  responses: Seq[QuestionAnswers]
)
  extends MongoDocument[SurveyInstance] {
  def meta = SurveyInstance
}

object SurveyInstance extends MongoDocumentMeta[SurveyInstance] {
  override def collectionName = "surveyInstance"
  override def formats = super.formats + new ObjectIdSerializer + new DateSerializer + new SurveyInstanceStatusSerializer
}

class SurveyInstanceStatusSerializer extends Serializer[SurveyInstanceStatus] {

  private val SurveyInstanceStatusClass = classOf[SurveyInstanceStatus]

  def deserialize(implicit format: Formats) = {

    case (TypeInfo(SurveyInstanceStatusClass, _), json) =>
      val className = json.extract[String]
      Class.forName(className).getField("MODULE$").get().asInstanceOf[SurveyInstanceStatus]
  }

  def serialize(implicit format: Formats) = {
    case possibleSurveyInstanceStatus if SurveyInstanceStatusClass.isInstance(possibleSurveyInstanceStatus) &&
      possibleSurveyInstanceStatus.getClass.getName().endsWith("$") => possibleSurveyInstanceStatus.getClass.getName()
  }
}
