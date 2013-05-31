package com.riveramj.snippet

import com.riveramj.service.{SurveyService, QuestionService}

import net.liftweb.util.ClearClearable
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http.{SHtml, Templates}
import com.riveramj.util.Paths
import com.riveramj.model.Question
import net.liftweb.http.js.{JsCmds, JsCmd}


object SurveySnippet {
  lazy val menu = Menu.param[String]("survey","survey",
    Full(_),
    (id) => id
  ) / "survey" / * >>
  TemplateBox(() => Templates( "survey" :: Nil))
}

class SurveySnippet extends Loggable {
  import SurveySnippet._

  var newQuestion = ""



  def deleteQuestion(questionId: Long):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
    }

  }


  def createQuestion(surveyId: Long) = {
    QuestionService.createQuestion(QuestionService.nextQuestionNumber(surveyId), newQuestion, surveyId)
  }

  def questionList(question: Question) = {

    ".question *" #> question.question.get &
    ".question [id]" #> question.questionId.get &
    ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => deleteQuestion(question.questionId.get))
  }

  def render() = {

    val surveyId = menu.currentValue map {_.toLong} openOr 0L

    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.surveyName.get) &
    "#question-list" #> questions.map{ question =>
      questionList(question)
      } &
    "#new-question" #> SHtml.text(newQuestion, newQuestion = _) &
    "#create-question" #> SHtml.onSubmitUnit(() => createQuestion(surveyId))
  }
}
