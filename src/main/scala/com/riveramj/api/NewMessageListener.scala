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
          println("we got here1")
        case instances =>
          println("we got here2")
          val surveyInstance = instances.head
          println(surveyInstance + "oooooooooooo") 
          println(surveyInstance.currentQuestionId + " ========")
          surveyInstance.currentQuestionId map(AnswerService.verifyAnswer(response, _) match {
            case "-1" =>
              println("we got here11")
              SurveyInstanceService.answerNotFound(response, surveyInstance.currentQuestionId.get, surveyInstance._id)
            case answer =>
              println("we got here22")
              SurveyInstanceService.sendNextQuestion(surveyInstance._id)
          })
      }
      JString("ok")
    }
  })

}
