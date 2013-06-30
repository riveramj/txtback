package com.riveramj.snippet

import com.riveramj.service._
import net.liftweb.util.ClearClearable
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http.{S, SHtml, Templates}
import com.riveramj.model.Question
import com.riveramj.util.PathHelpers.loggedIn
import net.liftweb.http.js.{JsCmds, JsCmd}

object SurveySnippet {
  lazy val menu = Menu.param[String]("survey","survey",
    Full(_),
    (id) => id
  ) / "survey" / * >>
    //loggedIn >>
  TemplateBox(() => Templates( "survey" :: Nil))
}

class SurveySnippet extends Loggable {
  import SurveySnippet._

  var newAnswerNumber = ""
  var newAnswer = ""
  var newQuestion = ""
  var toPhoneNumber = ""


  val surveyId = menu.currentValue map {_.toLong} openOr 0L

  def deleteQuestion(questionId: Long):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
        //TODO: provide feedback on delete action
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
    AnswerService.createAnswer(newAnswerNumber.toInt, newAnswer, questionId)
  }

  def createQuestion(surveyId: Long) = {
    QuestionService.createQuestion(QuestionService.nextQuestionNumber(surveyId), newQuestion, surveyId)
  }

  def questionAndAnswers(question: Question) = {
    val answers = AnswerService.findAllAnswersByQuestionId(question.questionId.get)

    ".question *" #> question.question.get &
    ".question [id]" #> question.questionId.get &
    ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => deleteQuestion(question.questionId.get)) &
    ".answer" #> answers.map{ answer =>
      ".answer-number *" #> answer.answerNumber.get &
      ".answer-text *" #> answer.answer.get &
      ".answer-text [id]" #> answer.answerId.get &
      ".delete-answer [onclick]" #> SHtml.ajaxInvoke(() => deleteAnswer(answer.answerId.get))
    } &
    "#new-answer-number" #> SHtml.text("", newAnswerNumber = _) &
    "#new-answer" #> SHtml.text("", newAnswer = _) &
    "#create-answer" #> SHtml.onSubmitUnit(() => createAnswer(question.questionId.get))
  }

  def startSurvey:JsCmd = {
    SurveyService.startSurvey(surveyId,toPhoneNumber)
    S.notice("send-survey-notice", "Survey Sent") //TODO: validate it actually sent
  }


  def render() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.surveyName.get) &
    "#view-responses [href]" #> ("/survey/" + surveyId + "/responses") &
    "#question-list" #> questions.map{ question =>
        questionAndAnswers(question)
      } &
    "#new-question" #> SHtml.text(newQuestion, newQuestion = _) &
    "#phone-number" #> SHtml.ajaxText(toPhoneNumber, toPhoneNumber = _) &
    "#send-survey [onclick]" #> SHtml.ajaxInvoke(startSurvey _) &
    "#create-question" #> SHtml.onSubmitUnit(() => createQuestion(surveyId))
  }
}
