package com.riveramj.snippet

import com.riveramj.service._
import net.liftweb.util.{CssSel, ClearClearable}
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http._
import com.riveramj.model.{Answer, Question}
import com.riveramj.util.PathHelpers.loggedIn
import net.liftweb.http.js.{JE, JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.Noop
import net.liftweb.http.js.JE.{JsRaw, JsVal, JsVar}

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
  object editQuestionId extends RequestVar[Box[Long]](Empty)


  val surveyId = menu.currentValue map {_.toLong} openOr 0L

  def deleteQuestion(questionId: Long):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Full(true) =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
        //TODO: provide feedback on delete action
    }

  }

//  def createAnswer(questionId: Long) = {
//    val nextAnswerNumber = AnswerService.findNextAnswerNumber(questionId)
//    AnswerService.createAnswer(nextAnswerNumber, newAnswer, questionId)
//  }

  def createQuestion(surveyId: Long) = {
    QuestionService.createQuestion(QuestionService.nextQuestionNumber(surveyId), newQuestion, surveyId)
  }

  def questionAndAnswers(question: Question): CssSel = {
    val questionId = question.questionId.get
    val answers = AnswerService.findAllAnswersByQuestionId(questionId)

    ".question *" #> question.question.get &
    ".question [id]" #> question.questionId.get &
    ".edit-question [onclick]" #> SHtml.ajaxInvoke(() => {
        editQuestionId(Full(questionId))
        editQuestionId.is
        Noop
      }) &
    ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => deleteQuestion(questionId)) &
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
    JsCmds.Run("$('#" + answerId + "e').parent().remove()")
  }

  def saveQuestion(question: Box[Question], changedAnswers: List[(String, Long)], deletedAnswers: List[Long])() {
    println(changed + " foo")
    println("changed: " + changedAnswers)
    changedAnswers.foreach {
      case (newAnswer, answerId) => changeAnswer(newAnswer,answerId)
    }
    println("deleted: " + deletedAnswers)
    deletedAnswers.foreach(deleteAnswer(_))
    QuestionService.saveQuestion(question.openOrThrowException("Couldn't Save Question")) //TODO: dont throw nasty exception
  }
  var changed = ""

  def editQuestion() = {

    var changedAnswers: List[(String, Long)] = Nil
    var deleteAnswers: List[Long] = Nil

    "#edit-question" #> SHtml.idMemoize(renderer => {
      val editId = editQuestionId.is.openOr(0L) //TODO: dont reply on initialized data
      var question = QuestionService.getQuestionById(editId)
      val answers = AnswerService.findAllAnswersByQuestionId(editId)

      ".question " #> SHtml.text(question.map(_.question.get).openOr(""), questionText => question = question.map(q => q.question(questionText))) &
      ".answer" #> answers.map { answer =>
        val answerId = answer.answerId.get

        ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
        ".answer-number *" #> answer.answerNumber.get &
        ".answer-text" #> SHtml.text(answer.answer.get, changed = _ , "id" -> (answerId + "e")) //TODO: Move the answer save into the "saveQuestion" method
      } &
      "#reload-page [onclick]" #> SHtml.ajaxInvoke(renderer.setHtml _) & //TODO: drop the reload click
      "#confirm-edit" #> SHtml.onSubmitUnit(saveQuestion(question, changedAnswers, deleteAnswers))
    })
  }

  def render() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    editQuestionId(QuestionService.getFirstQuestion(surveyId).map(_.questionId.get))

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
