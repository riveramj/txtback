package com.riveramj.snippet

import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import com.riveramj.model.{QuestionType, Question}
import com.riveramj.snippet.SurveySnippet._
import com.riveramj.service.{SurveyService, AnswerService, QuestionService}
import net.liftweb.http.js.{JE, JsCmds, JsCmd}
import net.liftweb.util.ClearNodes
import org.bson.types.ObjectId
import com.riveramj.model.Question
import net.liftweb.common.Full

class EditQuestionSnippet {

  var toPhoneNumber = ""
  var changedAnswers: Map[ObjectId, String] = Map()
  var newAnswers: Map[ObjectId, String] = Map()
  var deleteAnswers: List[ObjectId] = Nil
  val surveyId = surveyIdRV.is openOrThrowException "Not Valid Survey"

  def changeAnswer(newAnswer: String, answerId: ObjectId) = {
//    AnswerService.changeAnswer(newAnswer, answerId)
  }

  def deleteAnswer(answerId: ObjectId, surveyId: ObjectId, questionId: ObjectId) = {
    SurveyService.deleteAnswerById(answerId, surveyId, questionId)
  }

  def createAnswer(newAnswer: String, questionId: ObjectId) = {
    println("================ new answer is:" + newAnswer)
    val nextAnswerNumber = AnswerService.findNextAnswerNumber(questionId)
    AnswerService.createAnswer(nextAnswerNumber, newAnswer, questionId)
  }

  def removeAnswer(answerId: ObjectId)(): JsCmd = {
    deleteAnswers ::= answerId
    JsCmds.Run("$('#" + answerId + "e').parent().remove()")
  }

  def saveQuestion(question: Box[Question])() = {
    println(question + " question passed in")
    println(newAnswers + " new ======")
    println(deleteAnswers + " delete ======")
    println(changedAnswers + " changed ======")

    changedAnswers.foreach {
      case (answerId, newAnswer) => changeAnswer(newAnswer, answerId)
    }
    newAnswers.foreach {
      case (_, newAnswer) => question.map(q => createAnswer(newAnswer, q._id))
    }
    deleteAnswers.foreach(deleteAnswer(_, surveyId, question.map(_._id) openOrThrowException("Bad Question")))
    QuestionService.saveQuestion(question.openOrThrowException("Couldn't Save Question"), surveyId) //TODO: dont throw nasty exception

    JE.JsRaw(
      "$('#edit-question').modal('hide');" +
        "location.reload();"
    ).cmd
  }

  def editQuestion() = {
    var editId: ObjectId = ObjectId.get
    var editQuestion: Box[Question] = Empty
    var answers: Map[ObjectId, String] = Map()

    def changeAnswer(answer: String, answerId: ObjectId) = {
      changedAnswers += (answerId -> answer)
      answer
    }
    def addAnswer(answer: String, questionId: ObjectId) = {
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
        newAnswers += (ObjectId.get -> "")
        renderer.setHtml()
      }

      def reloadEditQuestion() = {
        editId = editQuestionIdRV.is openOrThrowException "Bad Question"
        editQuestion = QuestionService.getQuestionById(editId)
        answers = QuestionService.findAnswersByQuestionId(editId).flatMap{ answer =>
          List(answer._id -> answer.answer)
        }.toMap

        renderer.setHtml()
      }

      def questionAnswers() = {

        editQuestion.map(_.questionType) match {
          case Full(QuestionType.choseOne) => {
            ".answer" #> answers.map { case (answerId, answer) =>
              ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
                ".answer-text" #> SHtml.text(answer, changeAnswer(_, answerId), "id" -> (answerId + "e"))
            } &
              ".new-answer" #> newAnswers.map { case (answerId, answer) =>
                ".delete-answer [onclick]" #> SHtml.ajaxInvoke(removeAnswer(answerId)) &
                  ".answer-text" #> SHtml.text(answer, addAnswer(_, answerId), "id" -> (answerId + "e"))
              } &
              "#add-answer" #> SHtml.ajaxSubmit("Add Answer", addNewAnswer())
          }
          case _ =>
            ".answer" #> ClearNodes &
            ".new-answer" #> ClearNodes &
            "#add-answer" #> ClearNodes
        }
      }

      "#question" #> SHtml.text(editQuestion.map(_.question).openOr(""), questionText => editQuestion = editQuestion.map(q => q)) &
      "#questionType" #> "Question Type: %s".format(editQuestion.map(_.questionType).openOr("")) &
      questionAnswers &
      "#cancel-edit [onclick]" #> SHtml.ajaxInvoke(() => deleteAnswers = Nil ) &
      "#reload-page [onclick]" #> SHtml.ajaxInvoke(reloadEditQuestion) & //TODO: drop the reload click
      "#confirm-edit" #> SHtml.ajaxSubmit("Save Changes", saveQuestion(editQuestion))
    })
  }

}
