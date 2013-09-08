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
  }

  def getSurveyByName(surveyName: String): Box[Survey] = {
    Survey.find("name" -> surveyName)
  }

  def getAllSurveysByCompanyId(companyId: ObjectId): List[Survey] = {
    Survey.findAll("_id" -> companyId)
  }

  def getAllSurveys = {
    Survey.findAll
  }

  def startSurvey(surveyId: Long, toPhoneNumber: String) {
    val firstQuestion = QuestionService.getFirstQuestion(surveyId)
    SurveyInstanceService.createSurveyInstance(toPhoneNumber, surveyId, firstQuestion.map(_.questionId.get).openOr(0L))
    TwilioService.sendMessage(toPhoneNumber,questionToSend(firstQuestion))
  }
}
