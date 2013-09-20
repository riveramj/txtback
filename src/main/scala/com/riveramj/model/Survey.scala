package com.riveramj.model

import org.bson.types.ObjectId
import net.liftweb.mongodb.{ObjectIdSerializer, MongoDocumentMeta, MongoDocument}
import com.riveramj.service.SurveyService
import net.liftweb.common.Box
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.TypeInfo

sealed trait QuestionType
object QuestionType {
  case object trueFalse extends QuestionType
  case object ratingScale extends QuestionType
  case object freeResponse extends QuestionType
  case object choseOne extends QuestionType
}

case class Question(
  _id: ObjectId,
  question: String,
  questionType: QuestionType,
  questionNumber: Int,
  answers: List[Answer]
)

case class Answer(
  _id: ObjectId,
  answerNumber: Int,
  answer: String
)

case class Survey(
  _id: ObjectId,
  name: String,
  companyId: ObjectId,
  questions: List[Question]

)
  extends MongoDocument[Survey] {
  def meta = Survey
}

object Survey extends MongoDocumentMeta[Survey] {
  override def collectionName = "survey"
  override def formats = super.formats + new ObjectIdSerializer + new QuestionTypeSerializer

  def getQuestionById(questionId: ObjectId): Box[Question] = {
    val survey = Survey.find("question._id" -> ("$oid" -> questionId.toString))
    val question  = survey.map(_.questions.filter(_._id == questionId)) getOrElse Nil
    question.headOption

    //    {
    //      for {
    //        survey <- surveys
    //        question <- survey.questions
    //          if question._id == questionId
    //      }
    //      yield
    //        Full(question)
    //    }.head
  }

  def saveQuestion(question: Question, surveyId: ObjectId): Box[Question] = {
    val survey = SurveyService.getSurveyById(surveyId)
    val existingQuestions = survey.map(_.questions) openOr Nil
    println(existingQuestions + " ==-=-==-=-=-=-=-")
    survey.map { actualSurvey =>
      val updatedSurvey = actualSurvey.copy(questions = existingQuestions :+ question)
      println(updatedSurvey + " ===---=-=-=-=-=--2")
      updatedSurvey.save
    }
    getQuestionById(question._id)
  }

}

class QuestionTypeSerializer extends Serializer[QuestionType] {

  private val QuestionTypeClass = classOf[QuestionType]

  def deserialize(implicit format: Formats) = {

    case (TypeInfo(QuestionTypeClass, _), json) =>
      val className = json.extract[String]
      Class.forName(className).getField("MODULE$").get().asInstanceOf[QuestionType]
  }

  def serialize(implicit format: Formats) = {
    case possibleQuestionType if QuestionTypeClass.isInstance(possibleQuestionType) &&
      possibleQuestionType.getClass.getName().endsWith("$") => possibleQuestionType.getClass.getName()
  }
}
