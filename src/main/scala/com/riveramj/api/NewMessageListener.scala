package com.riveramj.api

import net.liftweb.http.rest._
import net.liftweb.common.{Empty, Loggable, Full}
import net.liftweb.json.JsonAST.JString
import com.riveramj.service._

object NewMessageListener extends RestHelper with Loggable {

  serve("api" / "textMessage" prefix {
    case "newMessage"  :: _ Post req =>
    {
      logger.info("new message is:")      
      logger.info(req)

      println("=========")
      println(req.body)
      println("=========")

      val fromPhone = PhoneNumberService.stripNonNumeric(req.param("From").openOr(""))
      
      val toPhone = PhoneNumberService.stripNonNumeric(req.param("To").openOr(""))

      val response = req.param("Body") openOr ""

      val surveyInstances = SurveyInstanceService.findOpenSurveyInstancesByPhone(fromPhone, toPhone)
      surveyInstances match {
        case instances if instances.isEmpty =>
          JString("no matches")
        case instances =>
          val surveyInstance = instances.head
          surveyInstance.currentQuestionId map(AnswerService.verifyAnswer(response, _) match {
            case "-1" =>
              SurveyInstanceService.answerNotFound(response, surveyInstance.currentQuestionId.get, surveyInstance._id)
              JString("answer not found")
            case answer =>
              SurveyInstanceService.recordAnswer(surveyInstance, response)
              SurveyInstanceService.sendNextQuestion(surveyInstance._id)
              JString("ok. next question sent")
          })
      }
    }
  })

}
