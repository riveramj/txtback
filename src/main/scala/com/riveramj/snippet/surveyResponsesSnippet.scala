package com.riveramj.snippet

import net.liftweb.sitemap._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http.Templates
import com.riveramj.service.{QuestionService, QASetService, SurveyInstanceService, SurveyService}
import net.liftweb.util.ClearClearable
import com.riveramj.model.QASet

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

  val surveyId = menu.currentValue map {_.toLong} openOr 0L
  val survey = SurveyService.getSurveyById(surveyId)

  def showQASetDetails(qaSets: List[QASet]) = {
    ".qa-set" #> qaSets.map{ qaSet =>
      findQuestionInfo(qaSet.QuestionId.get) &
      ".answer *" #> qaSet.answer.get &
      ".date-answered *" #> qaSet.dateAnswered.get.toString
    }
  }


  def findQuestionInfo(questionId: Long) = {
    val question = QuestionService.getQuestionById(questionId)
    ".question-number *" #> question.map(_.questionNumber.get) &
    ".question *" #> question.map(_.question.get)
  }

  def perSurveyResults() = {
    val allSurveyInstances = SurveyInstanceService.findAllSurveyInstancesBySurveyId(surveyId)

    ClearClearable andThen
    ".survey-instance" #> allSurveyInstances.map{ surveyInstance =>
      ".phone-number *" #> surveyInstance.responderPhone.get &
        ".status *" #> surveyInstance.status.get &
        ".date-started *" #> surveyInstance.dateStarted.get.toString &
        showQASetDetails(QASetService.findAllQASetsBySurveyInstance(surveyInstance.surveyInstanceId.get))
    }
  }

  def perQuestionResults() = {
    val allQASets = QASetService.findAllQASetsBySurveyId(surveyId)

    ClearClearable andThen
    ".questions" #> showQASetDetails(allQASets)
  }

  def render() = {
    ClearClearable andThen
    "#survey-name *" #> survey.map(_.surveyName.get) &
    "#view-survey [href]" #> ("/survey/" + surveyId)
  }

}
