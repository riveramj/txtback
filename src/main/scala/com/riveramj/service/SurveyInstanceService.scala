package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.SurveyInstance
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import com.riveramj.util.RandomIdGenerator._


object SurveyInstanceService extends Loggable {

  def createSurveyInstance(responderPhone:String, surveyId:Long) = {
    val surveyInstance = SurveyInstance.create
      .responderPhone(responderPhone)
      .surveyInstanceId(generateLongId())
      .SurveyId(surveyId)

    tryo(saveSurveyInstance(surveyInstance)) flatMap {
      u => u match {
        case Full(newSurveyInstance:SurveyInstance) => Full(newSurveyInstance)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
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

  def findAllSurveySurveyInstances(surveyInstanceId:Long): List[SurveyInstance] = {
    SurveyInstance.findAll(By(SurveyInstance.surveyInstanceId, surveyInstanceId))
  }

  def getAllSurveyInstances = {
    SurveyInstance.findAll()
  }

}
