package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import com.riveramj.util.SecurityContext
import com.riveramj.service.SurveyService
import net.liftweb.util.ClearClearable
import net.liftweb.http.SHtml
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.common.{Loggable, Full}
import org.bson.types.ObjectId

object Surveys {
  val menu = Menu.i("surveys") / "surveys"
}

class Surveys extends Loggable {
  val currentCompany = SecurityContext.currentCompany
  val currentCompanyId = SecurityContext.currentCompanyId openOrThrowException "Not valid Company"

  def list() = {
    val surveys = SurveyService.getAllSurveysByCompanyId(currentCompanyId)

    def deleteSurvey(surveyId: ObjectId): JsCmd = {
      SurveyService.deleteSurveyById(surveyId) match {
        case true =>
          JsCmds.Run("$('#" + surveyId + "').parent().remove()")
        case _ => logger.error("couldn't delete survey with id %s" format surveyId)
      }
    }

    ClearClearable andThen
    "#company-name *" #> currentCompany.map(_.name) &
    ".survey" #> surveys.map{ survey =>
      "a *" #> survey.name &
      "a [href]" #> ("/survey/" + survey._id) &
      "a [id]" #> survey._id.toString &
        ".delete-survey [onclick]" #> SHtml.ajaxInvoke(() => {
          JsCmds.Confirm("Are you sure you want to delete the question?", {
            SHtml.ajaxInvoke(() => {
              deleteSurvey(survey._id)
            }).cmd
          })
        })
    }
  }

  def create() = {
    var surveyName = ""

    def createSurvey() = {
      SurveyService.createSurvey(surveyName,currentCompanyId)
    }

    "#survey-name" #> SHtml.text(surveyName,surveyName = _) &
    "#create-survey" #> SHtml.onSubmitUnit(createSurvey _)
    
  }
}
