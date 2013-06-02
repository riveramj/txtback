package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import com.riveramj.util.SecurityContext
import com.riveramj.service.SurveyService
import net.liftweb.util.ClearClearable
import net.liftweb.http.SHtml
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.common.{Loggable, Full}

object Home {
  val menu = Menu.i("home") / "home"
}

class Home extends Loggable {

  def render() = {

    var surveyName = ""

    val currentCompany = SecurityContext.currentCompany
    val currentCompanyId = SecurityContext.currentCompanyId.openOr(0L)
    val surveys = SurveyService.getAllSurveysByCompanyId(currentCompanyId)

    def process() = {
      SurveyService.createSurvey(surveyName,currentCompanyId)
    }

    def deleteSurvey(surveyId: Long): JsCmd = {
      SurveyService.deleteSurveyById(surveyId) match {
        case Full(true) =>
          JsCmds.Run("$('#" + surveyId + "').parent().remove()")
        case _ => logger.error("couldn't delete survey with id %s" format surveyId)
      }

    }

    ClearClearable andThen
    "#company-name *" #> currentCompany.map(_.companyName.get) &
    ".survey" #> surveys.map{ survey =>
      "a *" #> survey.surveyName.get &
      "a [href]" #> ("/survey/" + survey.surveyId.get) &
      "a [id]" #> survey.surveyId.get &
      ".delete-survey [onclick]" #> SHtml.ajaxInvoke(() => deleteSurvey(survey.surveyId.get))
    } &
    "#survey-name" #> SHtml.text(surveyName,surveyName = _) &
    "#create-survey" #> SHtml.onSubmitUnit(process _)
  }
}
