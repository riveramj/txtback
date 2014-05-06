package com.riveramj.service

import com.riveramj.model._

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JObject

import org.bson.types.ObjectId

import java.util.regex._
import java.util.Date

object SurveyService extends Loggable {

  def createSurvey(name: String, userId: ObjectId) = {
    val survey = Survey(
      _id = ObjectId.get,
      name = name,
      userId = userId,
      questions = Nil,
      startedDate = None
    )

    saveSurvey(survey)
  }

  def markSurveyAsStarted(surveyId: ObjectId) = {
    val possibleSurvey = getSurveyById(surveyId)

    possibleSurvey.map { survey =>
      updateSurvey(survey.copy(
        startedDate = Some(new Date())
      ))
    }
  }

  def saveSurvey(survey:Survey): Box[Survey] = {
    survey.save
    Survey.find(survey._id)
  }

  def updateSurvey(query: JObject, survey: Survey) = {
    Survey.update(query, survey)
  }

  def updateSurvey(survey: Survey) = {
    Survey.update("_id" -> ("$oid" -> survey._id.toString), survey)
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
    val pattern = Pattern.compile(surveyName, Pattern.CASE_INSENSITIVE)
    Survey.find("name" -> (("$regex" -> pattern.pattern) ~ ("$flags" -> pattern.flags)))
  }

  def getAllSurveysByUserId(userId: ObjectId): List[Survey] = {
    Survey.findAll("userId" -> ("$oid" -> userId.toString))
  }

  def getAllSurveys = {
    Survey.findAll
  }

  def getAllQuestionsBySurveyId(surveyId: ObjectId): Seq[Question] = {
    getSurveyById(surveyId).map(_.questions.sortBy(_.questionNumber)) openOr Nil
  }

  def getFirstQuestionBySurveyId(surveyId: ObjectId): Box[Question] = {
    val question = getSurveyById(surveyId).map(_.questions.filter(_.questionNumber == 1)) openOr Nil
    question.headOption
  }

  def getSurveyByQuestionId(questionId: ObjectId): Box[Survey] = {
    Survey.find("questions._id" -> ("$oid" -> questionId.toString))
  }

  def deleteAnswerById(answerId: ObjectId, surveyId: ObjectId, questionId: ObjectId) = {
    val question = QuestionService.getQuestionById(questionId) openOrThrowException "Not valid question"
    val updatedQuestion = question.copy(answers = question.answers.filter(_._id != answerId))
    val survey = getSurveyById(surveyId) openOrThrowException "Not Valid Survey"
    updateSurvey(
      "questions._id" -> ("$oid" -> questionId.toString),
      survey.copy(
        questions = survey.questions.filter(_._id != questionId) :+ updatedQuestion
      )
    )
  }
}
