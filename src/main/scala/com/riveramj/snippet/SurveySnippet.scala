package com.riveramj.snippet

import com.riveramj.service._
import net.liftweb.util.{CssSel, ClearClearable}
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http._
import com.riveramj.model.Question
import net.liftweb.http.js.{JE, JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.Noop

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

  var newQuestion = ""
  var toPhoneNumber = ""
  var changedAnswers: Map[Long, String] = Map()
  var newAnswers: Map[Long, String] = Map()
  var deleteAnswers: List[Long] = Nil

  object editQuestionIdRV extends RequestVar[Box[Long]](Empty)
  object changedAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
  object newAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
  object deleteAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)


  val surveyId = menu.currentValue map {_.toLong} openOr 0L

  def deleteQuestion(questionId: Long):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
        //TODO: provide feedback on delete action
    }
  }

  def createAnswer(newAnswer: String, questionId: Long) = {
    val nextAnswerNumber = AnswerService.findNextAnswerNumber(questionId)
    AnswerService.createAnswer(nextAnswerNumber, newAnswer, questionId)
  }

  def createQuestion(surveyId: Long, questionType: String) = {
    QuestionService.createQuestion(QuestionService.nextQuestionNumber(surveyId), newQuestion, questionType, surveyId)
  }

  def questionAndAnswers(question: Question): CssSel = {
    val questionId = question.questionId.get
    val answers = AnswerService.findAllAnswersByQuestionId(questionId)

    ".question *" #> question.question.get &
    ".question [id]" #> question.questionId.get &
    ".edit-question [onclick]" #> SHtml.ajaxInvoke(() => {
        editQuestionIdRV(Full(questionId))
        editQuestionIdRV.is
        Noop
      }) &
    ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => {
      JsCmds.Confirm("Are you sure you want to delete the question?", {
        SHtml.ajaxInvoke(() => {
          deleteQuestion(questionId)
        }).cmd
      })
    }) &
    ".answer" #> answers.map{ answer =>
      ".answer-number *" #> answer.answerNumber.get &
      ".answer-text *" #> answer.answer.get &
      ".answer-text [id]" #> answer.answerId.get
    }
  }

  def startSurvey:JsCmd = {
    SurveyService.startSurvey(surveyId,toPhoneNumber)
    S.notice("send-survey-notice", "Survey Sent") //TODO: validate it actually sent
  }

  def changeAnswer(newAnswer: String, answerId: Long) = {
    AnswerService.changeAnswer(newAnswer, answerId)
  }

  def deleteAnswer(answerId: Long) = {
    AnswerService.deleteAnswerById(answerId)
  }

  def removeAnswer(answerId: Long)(): JsCmd = {
    deleteAnswers ::= answerId
    JsCmds.Run("$('#" + answerId + "e').parent().remove()")
  }

  def render() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    editQuestionIdRV(QuestionService.getFirstQuestion(surveyId).map(_.questionId.get))
    editQuestionIdRV.is

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.surveyName.get) &
    "#view-responses [href]" #> ("/survey/" + surveyId + "/responses") &
    "#question-list" #> questions.map{ question =>
        questionAndAnswers(question)
      } &
    "#new-question" #> SHtml.text(newQuestion, newQuestion = _) &
    "#phone-number" #> SHtml.ajaxText(toPhoneNumber, toPhoneNumber = _) &
    "#send-survey [onclick]" #> SHtml.ajaxInvoke(startSurvey _) &
    "#multiple-choice" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "multipleChoice"))
    "#true-false" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "trueFalse"))
    "#rating-scale" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "ratingScale"))
    "#free-response" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "freeResponse"))
  }
}
