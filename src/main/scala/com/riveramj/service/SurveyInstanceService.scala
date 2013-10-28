package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import com.riveramj.service.QuestionService.questionToSend
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction


object SurveyInstanceService extends Loggable {

  def createSurveyInstance(responderPhone: String, surveyId: ObjectId, currentQuestionId: ObjectId) = {
    val surveyInstance = SurveyInstance(
      _id = ObjectId.get(),
      surveyId = surveyId,
      responderPhone = responderPhone,
      status = SurveyInstanceStatus.Active,
      currentQuestionId = Some(currentQuestionId),
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
    SurveyInstance.findAll("surveyId" -> ("$oid" -> surveyId.toString))
  }

  def findOpenSurveyInstancesByPhone(phone: String): List[SurveyInstance] = {
    // TODO: the below should probably work. for now this will be a work around
    // implicit val formats = net.liftweb.json.DefaultFormats 
//     SurveyInstance.findAll(("responderPhone" -> phone) ~ ("status" -> Extraction.decompose(SurveyInstanceStatus.Active)))
   val surveys = SurveyInstance.findAll("responderPhone" -> phone)
   surveys.filter(_.status == SurveyInstanceStatus.Active)
  }

  def getAllSurveyInstances = {
    SurveyInstance.findAll
  }

  def sendNextQuestion(surveyInstanceId: ObjectId) {
    val surveyInstance = getSurveyInstanceById(surveyInstanceId) openOrThrowException "bad id"
    println(surveyInstance + " fooooo")
    val nextQuestion = surveyInstance.nextQuestionId.flatMap(QuestionService.getQuestionById(_))
    println(nextQuestion + " barrrr")
    val messageBody = nextQuestion match {
      case None =>
        SurveyInstanceService.finishSurveyInstance(surveyInstance)
        "Thank you for completing our survey. " +
          "To create your own text message survy, visit txtbck.co"
      case Some(question) =>
        questionToSend(Full(question))
    }

    TwilioService.sendMessage(
      toPhoneNumber = surveyInstance.responderPhone,
      message = messageBody
    )
    val updatedInstance = QuestionService.updateCurrentNextQuestionId(surveyInstance)
    saveSurveyInstance(updatedInstance) //todo: Update next question id
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

  def updateSurveyInstance(surveyInstance: SurveyInstance) = {
    SurveyInstance.update("_id" -> ("$oid" -> surveyInstance._id.toString), surveyInstance)
  }
}
