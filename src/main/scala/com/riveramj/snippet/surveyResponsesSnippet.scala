package com.riveramj.snippet

import net.liftweb.sitemap._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http.Templates
import com.riveramj.service.{QuestionService, QASetService, SurveyInstanceService, SurveyService}
import net.liftweb.util.ClearClearable

object SurveyResponsesSnippet {
  lazy val menu = Menu.param[String]("responses","responses",
    Full(_),
    (id) => id
  ) / "survey" / * / "responses" >>
    //loggedIn >>
    TemplateBox(() => Templates("responses" :: Nil))
}

class SurveyResponsesSnippet extends Loggable {
  import SurveyResponsesSnippet._

  def showSurveyInstanceDetails(surveyInstanceId: Long) = {
    ".qa-set" #> QASetService.findAllQASetsBySurveyInstance(surveyInstanceId).map{ qaSet =>
      ".question-number" #> QuestionService.getQuestionById(qaSet.QuestionId.get).map(_.questionNumber.get) &
      ".question" #> QuestionService.getQuestionById(qaSet.QuestionId.get).map(_.question.get) &
      ".answer" #> qaSet.answer.get &
      ".date-answered" #> qaSet.dateAnswered.get.toString
    }
  }

  def render() = {

    val surveyId = menu.currentValue map {_.toLong} openOr 0L
    val survey = SurveyService.getSurveyById(surveyId)
    val allSurveyInstances = SurveyInstanceService.findAllSurveyInstancesBySurveyId(surveyId)

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.surveyName.get) &
    "#view-survey [href]" #> ("/survey/" + surveyId) &
    ".survey-instance" #> allSurveyInstances.map{ surveyInstance =>
      ".phone-number" #> surveyInstance.responderPhone.get &
      ".status" #> surveyInstance.status.get &
      ".date-started" #> surveyInstance.dateStarted.get.toString &
      showSurveyInstanceDetails(surveyInstance.id.get)
    }

  }

}
