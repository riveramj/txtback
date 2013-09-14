package com.riveramj.api

import net.liftweb.http.rest._
import net.liftweb.common.{Empty, Loggable, Full}
import net.liftweb.json.JsonAST.JString
import com.riveramj.service._

object NewMessageListener extends RestHelper with Loggable {

  serve("api" / "textMessage" prefix {
    case "newMessage"  :: _ Post req =>
    {
      val fromPhone = req.param("From") match {
        case Full(phone) if phone.startsWith("+1") => phone.substring(2)
        case Full(phone) => phone
      }
      val response = req.param("Body") openOr ""

      val surveyInstances = SurveyInstanceService.findOpenSurveyInstancesByPhone(fromPhone)
      surveyInstances match {
        case instances if instances.isEmpty =>
        case instances =>
          val surveyInstance = instances.head
          surveyInstance.currentQuestionId map(SurveyInstanceService.verifyAnswer(response, _) match {
            case "-1" =>
              SurveyInstanceService.answerNotFound(response, surveyInstance.currentQuestionId.get, surveyInstance.surveyInstanceId.get)
            case answer =>
              SurveyInstanceService.sendNextQuestion(surveyInstance._id)
          })
      }
      JString("ok")
    }
  })

}