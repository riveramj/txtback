package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.SurveyInstance
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import com.riveramj.util.RandomIdGenerator._
import org.joda.time.DateTime
import com.riveramj.service.QuestionService.questionToSend


object SurveyInstanceService extends Loggable {

  def createSurveyInstance(responderPhone:String, surveyId:Long, questionId:Long) = {
    val surveyInstance = SurveyInstance.create
      .responderPhone(responderPhone)
      .surveyInstanceId(generateLongId())
      .SurveyId(surveyId)
      .status(1)
      .currentQuestionId(questionId)
      .dateStarted(DateTime.now().toDate)

    tryo(saveSurveyInstance(surveyInstance)) flatMap {
      u => u match {
        case Full(newSurveyInstance:SurveyInstance) => Full(newSurveyInstance)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def updateSurveyInstance(surveyInstance: Box[SurveyInstance]) = {
    surveyInstance flatMap { newInstance =>
      tryo(saveSurveyInstance(newInstance)) flatMap {
        u => u match {
          case Full(newSurveyInstance:SurveyInstance) => Full(newSurveyInstance)
          case (failure: Failure) => failure
          case _ => Failure("Unknown error")
        }
      }
    }
  }

  def finishSurveyInstance(surveyInstance: Box[SurveyInstance]) = {
    surveyInstance.map { currentInstance =>
      saveSurveyInstance(currentInstance.status(2))
    }
  }

  def saveSurveyInstance(surveyInstance:SurveyInstance):Box[SurveyInstance] = {

    val validateErrors = surveyInstance.validate

    if (validateErrors.isEmpty) {
      tryo(surveyInstance.saveMe()) match {
        case Full(newSurveyInstance:SurveyInstance) =>
          Full(newSurveyInstance)
        case Failure(_, Full(err), _) =>
          logger.error("save surveyInstance failed with %s" format err)
          Failure("Unknown error")
        case _ =>
          Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getSurveyInstanceById(surveyInstanceId: Long): Box[SurveyInstance] = {
    SurveyInstance.find(By(SurveyInstance.surveyInstanceId, surveyInstanceId))
  }

  def deleteSurveyInstanceById(surveyInstanceId: Long): Box[Boolean] = {
    val surveyInstance = SurveyInstance.find(By(SurveyInstance.surveyInstanceId, surveyInstanceId))
    surveyInstance.map(_.delete_!)
  }

  def findAllSurveyInstancesBySurveyId(surveyId: Long): List[SurveyInstance] = {
    SurveyInstance.findAll(By(SurveyInstance.SurveyId, surveyId))
  }

  def findOpenSurveyInstancesByPhone(phone:String): List[SurveyInstance] = {
    SurveyInstance.findAll(By(SurveyInstance.responderPhone, phone), By(SurveyInstance.status, 1))
  }

  def getAllSurveyInstances = {
    SurveyInstance.findAll
  }

  def sendNextQuestion(surveyInstanceId: Long) {
    val surveyInstance = getSurveyInstanceById(surveyInstanceId)
    val currentQuestionId = surveyInstance map(_.currentQuestionId.get)
    val currentQuestionNumber = QuestionService.getQuestionById(currentQuestionId openOr -1L).map(_.questionNumber.get) openOr -1L
    val nextQuestion = QuestionService.findQuestionByNumber(currentQuestionNumber + 1)

    val messageBody = nextQuestion match {
      case Empty =>
        SurveyInstanceService.finishSurveyInstance(surveyInstance)
        "Thank you for completing our survey. " +
          "To create your own text message survy, visit txtbck.co"
      case Full(question) =>
        questionToSend(Full(question))
    }
    TwilioService.sendMessage(
      toPhoneNumber = surveyInstance.map(_.responderPhone.get) openOr(""),
      message = messageBody
    )
    SurveyInstanceService.updateSurveyInstance(surveyInstance map { instance =>
      instance.currentQuestionId(
        nextQuestion.map(_.questionId.get) openOr 0L
      )
    })
  }

  def answerNotFound(response: String, questionId: Long, surveyInstanceId: Long) = {
    val surveyInstance = getSurveyInstanceById(surveyInstanceId)
    QuestionService.getQuestionById(questionId) match {
      case Full(question) => {
        TwilioService.sendMessage(
          toPhoneNumber = surveyInstance.map(_.responderPhone.get) openOr(""),
          message = "We did not understand your previous answer of \"" + response + "\"."
        )
        TwilioService.sendMessage(
          toPhoneNumber = surveyInstance.map(_.responderPhone.get) openOr(""),
          message = question.question.get
        )
      }
    }
  }
}
