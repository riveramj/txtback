package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.common.Full


object SurveyService extends Loggable {

  def createSurvey(name:String, companyId:Long) = {
    val survey = Survey.create
      .surveyName(name)
      .companyId(companyId)
      .surveyId(generateLongId())

    tryo(saveSurvey(survey)) flatMap {
      u => u match {
        case Full(newSurvey:Survey) => Full(newSurvey)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def saveSurvey(survey:Survey):Box[Survey] = {

    val uniqueConstraintPattern = """.*Unique(.+)""".r
    val validateErrors = survey.validate

    if (validateErrors.isEmpty) {
      tryo(survey.saveMe()) match {
        case Full(newSurvey:Survey) => Full(newSurvey)
        case Failure(_, Full(err), _) => {
          val error = err.getMessage.substring(0, err.getMessage.indexOf("\n"))
          error match {
            case uniqueConstraintPattern(x) => Failure("Survey Already Exists")
            case _ => Failure("Unknown error")
          }
        }
        case _ => Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getSurveyById(surveyId: Long): Box[Survey] = {
    Survey.find(By(Survey.surveyId, surveyId))
  }

  def deleteSurveyById(surveyId: Long ): Box[Boolean] = {
    val survey = Survey.find(By(Survey.surveyId, surveyId))
    val questions = QuestionService.findAllSurveyQuestions(surveyId)
    questions.map(_.delete_!)
    survey.map(_.delete_!)
  }

  def getSurveyByName(surveyName: String): Box[Survey] = {
    Survey.find(By(Survey.surveyName, surveyName))
  }

  def getAllSurveysByCompanyId(companyId: Long): List[Survey] = {
    Survey.findAll(By(Survey.companyId, companyId))
  }

  def getAllSurveys = {
    Survey.findAll()
  }

  def startSurvey(surveyId: Long, toPhoneNumber: String) {
    SurveyInstanceService.createSurveyInstance(toPhoneNumber, surveyId)
    val firstQuestion = QuestionService.getFirstQuestion(surveyId)
    TwilioService.sendMessage(toPhoneNumber,firstQuestion)
  }
}
