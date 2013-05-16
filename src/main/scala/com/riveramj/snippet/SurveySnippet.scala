package com.riveramj.snippet

import com.riveramj.service.{AnswerService, SurveyService, QuestionService}

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

  var newAnswer = ""
  var newQuestion = ""

  def deleteQuestion(questionId: Long):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
    }

  }

  def deleteAnswer(answerId: Long):JsCmd = {
    AnswerService.deleteAnswerById(answerId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + answerId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format answerId)
    }

  }

  def createAnswer(questionId: Long) = {
    AnswerService.createAnswer(List(newAnswer), questionId)
  }

  def createQuestion(surveyId: Long) = {
    QuestionService.createQuestion(newQuestion, surveyId)
  }

  def questionAndAnswers(question: Question) = {
    val answers = AnswerService.findAllAnswersByQuestionId(question.questionId.get)
    var answer = ""

    ".question *" #> question.question.get &
    ".question [id]" #> question.questionId.get &
    ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => deleteQuestion(question.questionId.get)) &
    ".answer" #> answers.map{ answer =>
      "span *" #> answer.answer.get &
      "span [id]" #> answer.answerId.get &
      ".delete-answer [onclick]" #> SHtml.ajaxInvoke(() => deleteAnswer(answer.answerId.get))
    } &
    "#new-answer" #> SHtml.text("", answer = _) &
    "#create-answer" #> SHtml.onSubmitUnit(() => createAnswer(question.questionId.get))
  }

  def render() = {

    val surveyId = menu.currentValue map {_.toLong} openOr 0L

    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.surveyName.get) &
    "#question-list" #> questions.map{ question =>
        questionAndAnswers(question)
      } &
    "#new-question" #> SHtml.text(newQuestion,newQuestion = _) &
    "#create-question" #> SHtml.onSubmitUnit(() => createQuestion(surveyId))
  }
}
