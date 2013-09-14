package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.{SurveyInstanceStatus, SurveyInstance}
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import com.riveramj.service.QuestionService.questionToSend
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._



object SurveyInstanceService extends Loggable {

  def createSurveyInstance(responderPhone: String, surveyId: ObjectId, questionId: ObjectId) = {
    val surveyInstance = SurveyInstance(
      _id = ObjectId.get(),
      surveyId = surveyId,
      responderPhone = responderPhone,
      status = SurveyInstanceStatus.Active,
      responses = Nil
    )

    saveSurveyInstance(surveyInstance)
  }

  def finishSurveyInstance(surveyInstance: SurveyInstance) = {
    saveSurveyInstance(surveyInstance.copy(status = SurveyInstanceStatus.Finished))
  }

  def saveSurveyInstance(surveyInstance: SurveyInstance): Box[SurveyInstance] = {
    surveyInstance.save
    getSurveyInstanceById(surveyInstance._id)
  }

  def getSurveyInstanceById(surveyInstanceId: ObjectId): Box[SurveyInstance] = {
    SurveyInstance.find(surveyInstanceId)
  }

  def deleteSurveyInstanceById(surveyInstanceId: ObjectId) = {
    getSurveyInstanceById(surveyInstanceId) map(_.delete)
  }

  def findAllSurveyInstancesBySurveyId(surveyId: ObjectId): List[SurveyInstance] = {
    SurveyInstance.findAll("surveyId" -> surveyId)
  }

  def findOpenSurveyInstancesByPhone(phone: String): List[SurveyInstance] = {
    SurveyInstance.findAll(("responderPhone" -> phone) ~ ("status" -> SurveyInstanceStatus.Active))
  }

  def getAllSurveyInstances = {
    SurveyInstance.findAll
  }

  def sendNextQuestion(surveyInstanceId: ObjectId) {
    val surveyInstance = getSurveyInstanceById(surveyInstanceId)
    val nextQuestionId = surveyInstance flatMap(_.nextQuestionId)
    val nextQuestion = nextQuestionId flatMap { questionId =>
      QuestionService.getQuestionById(questionId)
    }

    val messageBody = nextQuestion match {
      case Empty =>
        surveyInstance map(SurveyInstanceService.finishSurveyInstance(_))
        "Thank you for completing our survey. " +
          "To create your own text message survy, visit txtbck.co"
      case Full(question) =>
        questionToSend(Full(question))
    }
    TwilioService.sendMessage(
      toPhoneNumber = surveyInstance.map(_.responderPhone) openOr "",
      message = messageBody
    )
    surveyInstance.map{ instance =>
      saveSurveyInstance(instance.copy()) //todo: Update next question id
    }
  }

  def answerNotFound(response: String, questionId: ObjectId, surveyInstanceId: ObjectId) = {
    val surveyInstance = getSurveyInstanceById(surveyInstanceId)
    QuestionService.getQuestionById(questionId) match {
      case Full(question) => {
        TwilioService.sendMessage(
          toPhoneNumber = surveyInstance.map(_.responderPhone) openOr "",
          message = "We did not understand your previous answer of \"" + response + "\"."
        )
        TwilioService.sendMessage(
          toPhoneNumber = surveyInstance.map(_.responderPhone) openOr "",
          message = question.question
        )
      }
    }
  }
}
