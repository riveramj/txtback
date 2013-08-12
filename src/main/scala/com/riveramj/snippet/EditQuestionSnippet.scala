package com.riveramj.snippet

import net.liftweb.common.{Empty, Box}
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import com.riveramj.model.Question
import com.riveramj.snippet.SurveySnippet._
import com.riveramj.service.{AnswerService, QuestionService}
import net.liftweb.http.js.{JE, JsCmds, JsCmd}

class EditQuestionSnippet {

  var toPhoneNumber = ""
  var changedAnswers: Map[Long, String] = Map()
  var newAnswers: Map[Long, String] = Map()
  var deleteAnswers: List[Long] = Nil

  def changeAnswer(newAnswer: String, answerId: Long) = {
    AnswerService.changeAnswer(newAnswer, answerId)
  }

  def deleteAnswer(answerId: Long) = {
    AnswerService.deleteAnswerById(answerId)
  }

  def createAnswer(newAnswer: String, questionId: Long) = {
    val nextAnswerNumber = AnswerService.findNextAnswerNumber(questionId)
    AnswerService.createAnswer(nextAnswerNumber, newAnswer, questionId)
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
        editId = editQuestionIdRV.is.openOr(0L)
        question = QuestionService.getQuestionById(editId)
        answers = AnswerService.findAllAnswersByQuestionId(editId).flatMap{ answer =>
          List(answer.answerId.get -> answer.answer.get)
        }.toMap

        renderer.setHtml()
      }

      ".question " #> SHtml.text(question.map(_.question.get).openOr(""), questionText => question = question.map(q => q.question(questionText))) &
        ".answer" #> answers.map { case (answerId, answer) =>
          ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
            ".answer-text" #> SHtml.text(answer, changeAnswer(_, answerId), "id" -> (answerId + "e"))
        } &
        ".new-answer" #> newAnswers.map { case (answerId, answer) =>
          ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
            ".answer-text" #> SHtml.text(answer, addAnswer(_, answerId), "id" -> (answerId + "e"))
        } &
        "#add-answer" #> SHtml.ajaxSubmit("Add Answer", addNewAnswer()) &
        "#cancel-edit [onclick]" #> SHtml.ajaxInvoke(() => deleteAnswers = Nil ) &
        "#reload-page [onclick]" #> SHtml.ajaxInvoke(reloadEditQuestion) & //TODO: drop the reload click
        "#confirm-edit" #> SHtml.ajaxSubmit("Save Changes", saveQuestion(question))
    })
  }

}
