package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import com.riveramj.util.SecurityContext
import com.riveramj.service.SurveyService
import net.liftweb.util.ClearClearable
import net.liftweb.http.SHtml

object Home {
  val menu = Menu.i("Home") / "Home"
}

class Home {

  def render() = {

    var surveyName = ""

    val currentCompany = SecurityContext.currentCompany
    val currentCompanyId = SecurityContext.currentCompanyId.openOr(0L)
    val surveys = SurveyService.getAllSurveysByCompanyId(currentCompanyId)

    def process() = {
      SurveyService.createSurvey(surveyName,currentCompanyId)
    }

    ClearClearable andThen
    "#company-name *" #> currentCompany.map(_.companyName.get) &
    ".survey" #> surveys.map{ survey =>
      ".survey a *" #> survey.surveyName.get &
      ".survey a [href]" #> ("/survey/" + survey.surveyId.get)
    } &
    "#survey-name" #> SHtml.text(surveyName,surveyName = _) &
    "#create-survey" #> SHtml.onSubmitUnit(process _)
  }
}
