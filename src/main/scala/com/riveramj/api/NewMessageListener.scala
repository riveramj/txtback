package com.riveramj.api

import net.liftweb.http.rest._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.json.JsonAST.JString
import com.riveramj.service._

object NewMessageListener extends RestHelper with Loggable {

  serve("api" / "textMessage" prefix {
    case "newMessage"  :: _ Post req =>
    {
      val fromPhone = req.param("From") openOr ""
      val response = req.param("Body") openOr ""

      val surveyInstances = SurveyInstanceService.findOpenSurveyInstancesByPhone(fromPhone)
      val surveyInstance = surveyInstances.last
      QASetService.createQASet(surveyInstance.surveyInstanceId.get, surveyInstance.currentQuestionId.get, response)
      SurveyInstanceService.sendNextQuestion(surveyInstance.surveyInstanceId.get)

      JString("ok")

    }
  })

}