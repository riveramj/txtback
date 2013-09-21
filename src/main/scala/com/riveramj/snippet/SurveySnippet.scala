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
import org.bson.types.ObjectId

object SurveySnippet {
  lazy val menu = Menu.param[String]("survey","survey",
    Full(_),
    (id) => id
  ) / "survey" / * >>
    //loggedIn >>
  TemplateBox(() => Templates( "survey" :: Nil))

  object editQuestionIdRV extends RequestVar[Box[ObjectId]](Empty)
  object surveyIdRV extends RequestVar[Box[ObjectId]](Empty)
  object changedAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
  object newAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
  object deleteAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
}

class SurveySnippet extends Loggable {
  import SurveySnippet._

  var newQuestion = ""
  var toPhoneNumber = ""

  val surveyId = menu.currentValue map {ObjectId.massageToObjectId(_)} openOrThrowException "Not valid survey id"
  surveyIdRV(Full(surveyId))
  surveyIdRV.is

  def deleteQuestion(questionId: ObjectId):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
        //TODO: provide feedback on delete action
    }
  }

  def createQuestion(surveyId: ObjectId, questionType: String): JsCmd = {
    if(newQuestion.nonEmpty) {
//      QuestionService.createQuestion(QuestionService.nextQuestionNumber(surveyId), newQuestion, questionType, surveyId)
      S.notice("survey-question-created", "Question Created")
    }
    else
      S.error("survey-question-created", "Question is required")
  }

  def questionAndAnswers(question: Question): CssSel = {
    val questionId = question._id
    val answers = QuestionService.findAnswersByQuestionId(questionId)

    ".question *" #> question.question &
    ".question [id]" #> questionId.toString &
    ".edit-question [onclick]" #> SHtml.ajaxInvoke(() => {
        editQuestionIdRV(Full(questionId))
        editQuestionIdRV.is
        Noop
      }) &
//    ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => {
//      JsCmds.Confirm("Are you sure you want to delete the question?", {
//        SHtml.ajaxInvoke(() => {
//          deleteQuestion(questionId)
//        }).cmd
//      })
//    }) &
    ".answer" #> answers.map{ answer =>
      ".answer-number *" #> answer.answerNumber &
      ".answer-text *" #> answer.answer &
      ".answer-text [id]" #> answer._id.toString
    }
  }

  def startSurvey:JsCmd = {
    SurveyService.startSurvey(surveyId,toPhoneNumber)
    S.notice("send-survey-notice", "Survey Sent") //TODO: validate it actually sent
  }

  def render() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val questions = SurveyService.getAllQuestionsBySurveyId(surveyId)

    editQuestionIdRV(SurveyService.getFirstQuestionBySurveyId(surveyId).map(_._id))
    editQuestionIdRV.is

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.name) &
    "#view-responses [href]" #> ("/survey/" + surveyId + "/responses") &
    "#question-list" #> questions.map{ question =>
        questionAndAnswers(question)
      } &
    "#new-question" #> SHtml.text(newQuestion, newQuestion = _) &
    "#phone-number" #> SHtml.ajaxText(toPhoneNumber, toPhoneNumber = _) &
    "#send-survey [onclick]" #> SHtml.ajaxInvoke(startSurvey _) &
    "#chose-one" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "choseOne")) &
    "#true-false" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "trueFalse")) &
    "#rating-scale" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "ratingScale")) &
    "#free-response" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, "freeResponse"))
  }
}
