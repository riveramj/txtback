package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import net.liftweb.util.Helpers._
import com.riveramj.service.QuestionService.questionToSend
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._

object SurveyService extends Loggable {

  def createSurvey(name: String, companyId: ObjectId) = {
    val survey = Survey(
      _id = ObjectId.get,
      name = name,
      companyId = companyId,
      questions = Nil
    )

    saveSurvey(survey)
  }

  def saveSurvey(survey:Survey): Box[Survey] = {
    survey.save
    Survey.find(survey._id)
  }

  def getSurveyById(surveyId: ObjectId): Box[Survey] = {
    Survey.find(surveyId)
  }

  def deleteSurveyById(surveyId: ObjectId) = {
    val survey = getSurveyById(surveyId)
    survey.map(_.delete)
    getSurveyById(surveyId).isEmpty
  }

  def getSurveyByName(surveyName: String): Box[Survey] = {
    Survey.find("name" -> surveyName)
  }

  def getAllSurveysByCompanyId(companyId: ObjectId): List[Survey] = {
    Survey.findAll("_id" -> ("$oid" -> companyId.toString))
  }

  def getAllSurveys = {
    Survey.findAll
  }

  def getAllQuestionsBySurveyId(surveyId: ObjectId): List[Question] = {
    getSurveyById(surveyId).map(_.questions) openOr Nil
  }

  def getFirstQuestionBySurveyId(surveyId: ObjectId): Box[Question] = {
    val question = getSurveyById(surveyId).map(_.questions.filter(_.questionNumber == 1)) openOr Nil
    question.headOption
  }

  def startSurvey(surveyId: ObjectId, toPhoneNumber: String) {
    val firstQuestion = getFirstQuestionBySurveyId(surveyId)
    SurveyInstanceService.createSurveyInstance(
      toPhoneNumber,
      surveyId,
      firstQuestion.map(_._id) openOrThrowException "No first question")
    TwilioService.sendMessage(toPhoneNumber,questionToSend(firstQuestion))
  }

  def getSurveyByQuestionId(questionId: ObjectId): Box[Survey] = {
    Survey.find("questions._id" -> ("$oid" -> questionId.toString))
  }
}
