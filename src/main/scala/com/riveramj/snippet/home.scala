package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._
import com.riveramj.util.SecurityContext
import com.riveramj.service.SurveyService
import net.liftweb.util.ClearClearable

object Home {
  val menu = Menu.i("Home") / "Home"
}

class Home {

  def render() = {

    val currentCompany = SecurityContext.currentCompany
    val currentCompanyId = SecurityContext.currentCompanyId
    val surveys = SurveyService.getAllSurveysByCompanyId(currentCompanyId openOr 0)

    ClearClearable andThen
    "#company-name *" #> currentCompany.map(_.companyName.get) &
    "#survey-list" #> surveys.map{ survey =>
      ".survey *" #> survey.surveyName.get &
      ".survey [href]" #> ("/survey/" + survey.surveyId.get)
    }
  }
}
