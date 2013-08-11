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
import net.liftweb.http.js.JsCmds.{SetHtml, Alert, Noop}
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
  var changedAnswers: Map[Long, String] = Map()
  var newAnswers: Map[Long, String] = Map()
  var deleteAnswers: List[Long] = Nil

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

  def createAnswer(newAnswer: String, questionId: Long) = {
    val nextAnswerNumber = AnswerService.findNextAnswerNumber(questionId)
    AnswerService.createAnswer(nextAnswerNumber, newAnswer, questionId)
  }

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

  def saveQuestion(question: Box[Question])() = {

    println(changedAnswers + " changed")
    println(newAnswers + " new")
    println(deleteAnswers + " delete")

    changedAnswers.foreach {
      case (answerId, newAnswer) => changeAnswer(newAnswer, answerId)
    }
    newAnswers.foreach {
      case (_, newAnswer) => createAnswer(newAnswer, question.map(_.questionId.get).openOr(0L))
    }
    deleteAnswers.foreach(deleteAnswer(_))
    QuestionService.saveQuestion(question.openOrThrowException("Couldn't Save Question")) //TODO: dont throw nasty exception

    JE.JsRaw(
      "$('#edit-question').modal('hide');" +
      "location.reload();"
    ).cmd
  }

  def editQuestion() = {

    var editId: Long = 0L
    var question: Box[Question] = Empty
    var answers: Map[Long, String] = Map()

    def changeAnswer(answer: String, answerId: Long) = {
      changedAnswers += (answerId -> answer)
      answer
    }
    def addAnswer(answer: String, questionId: Long) = {
      newAnswers += (questionId -> answer)
      answer
    }

    "#edit-question" #> SHtml.idMemoize(renderer => {

      def addNewAnswer()() = {
        answers = changedAnswers
        deleteAnswers.foreach{ id =>
          answers = answers.filter{
            case(answerId, _) => answerId != id
          }
        }
        newAnswers += (newAnswers.size + 1L -> "")
        renderer.setHtml()
      }

      def reloadEditQuestion() = {
        editId = editQuestionId.is.openOr(0L)
        question = QuestionService.getQuestionById(editId)
        answers = AnswerService.findAllAnswersByQuestionId(editId).flatMap{ answer =>
          List(answer.answerId.get -> answer.answer.get)
        }.toMap
        renderer.setHtml()
      }

      ".question " #> SHtml.text(question.map(_.question.get).openOr(""), questionText => question = question.map(q => q.question(questionText))) &
      ".answer" #> answers.map { case (answerId, answer) =>
        ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
        ".answer-text" #> SHtml.text(answer, changeAnswer(_, answerId), "id" -> (answerId + "e")) //TODO: Move the answer save into the "saveQuestion" method
      } &
      ".new-answer" #> newAnswers.map { case (answerId, answer) =>
        ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
        ".answer-text" #> SHtml.text(answer, addAnswer(_, answerId), "id" -> (answerId + "e")) //TODO: Move the answer save into the "saveQuestion" method
      } &
      "#add-answer" #> SHtml.ajaxSubmit("Add Answer", addNewAnswer()) &
      "#cancel-edit [onclick]" #> SHtml.ajaxInvoke(()=> deleteAnswers = Nil ) &
      "#reload-page [onclick]" #> SHtml.ajaxInvoke(reloadEditQuestion) & //TODO: drop the reload click
      "#confirm-edit" #> SHtml.ajaxSubmit("Save Changes", saveQuestion(question))
    })
  }

  def render() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    editQuestionId(QuestionService.getFirstQuestion(surveyId).map(_.questionId.get))
    editQuestionId.is

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
