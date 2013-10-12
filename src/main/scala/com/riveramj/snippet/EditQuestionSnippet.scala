package com.riveramj.snippet

import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import com.riveramj.model.{Answer, QuestionType, Question}
import com.riveramj.snippet.SurveySnippet._
import com.riveramj.service.{SurveyService, AnswerService, QuestionService}
import net.liftweb.http.js.{JE, JsCmds, JsCmd}
import net.liftweb.util.ClearNodes
import org.bson.types.ObjectId
import net.liftweb.common.Full

class EditQuestionSnippet {

  val surveyId = surveyIdRV.is openOrThrowException "Not Valid Survey"

  def editQuestion() = {
    var editId: ObjectId = ObjectId.get
    var currentQuestion: Box[Question] = Empty
    var currentAnswers: Seq[Answer] = Nil

    def updateAnswer(newAnswer: String, answerId: ObjectId) = {
      currentQuestion = currentQuestion.map { question =>

        question.copy(answers = question.answers.collect {
          case answer if answer._id == answerId =>
            answer.copy(answer = newAnswer)
          case answer =>
            answer
        })
      }
      newAnswer
    }

    def saveQuestion()() = {
      QuestionService.saveQuestion(currentQuestion.openOrThrowException("Couldn't Save Question"), surveyId) //TODO: dont throw nasty exception

      JE.JsRaw(
        "$('#edit-question').modal('hide');" +
          "location.reload();"
      ).cmd
    }

    "#edit-question" #> SHtml.idMemoize(renderer => {

      def addNewAnswer()() = {
        currentQuestion = currentQuestion.map { question =>
          question.copy(answers =
            question.answers :+ Answer(
              _id = ObjectId.get ,
              answerNumber =   //TODO: This needs to be cleaned up
                if(currentAnswers.nonEmpty)
                  currentAnswers.last.answerNumber + 1
                else
                  1,
              answer = ""))
        }
        val q = currentQuestion openOrThrowException "Bad Question"
        currentAnswers = q.answers
        renderer.setHtml()
      }

      def removeAnswer(answerId: ObjectId)(): JsCmd = {
        currentQuestion = currentQuestion.map { question =>
          val removedAnswerNumber = question.answers.filter(_._id == answerId).map(_.answerNumber).head
          val updatedAnswers = question.answers.filter(_._id != answerId)
          val correctedAnswerNumbers = updatedAnswers.map { answer =>
            if(answer.answerNumber > removedAnswerNumber)
              answer.copy(answerNumber = answer.answerNumber - 1)
            else
              answer.copy(answerNumber = answer.answerNumber)
          }

          question.copy(answers = correctedAnswerNumbers)
        }
        val q = currentQuestion openOrThrowException "Bad Question"
        currentAnswers = q.answers
        renderer.setHtml()
      }

      def reloadEditQuestion() = {
        editId = editQuestionIdRV.is openOrThrowException "Bad Question"
        currentQuestion = QuestionService.getQuestionById(editId)
        currentAnswers = QuestionService.findAnswersByQuestionId(editId)
        renderer.setHtml()
      }

      def questionAnswers() = {
        currentQuestion.map(_.questionType) match {
          case Full(QuestionType.choseOne) => {
            ".answer" #> currentAnswers.map {
              answer =>
                ".delete-answer" #> SHtml.ajaxSubmit("-", removeAnswer(answer._id)) &
                ".question-number *" #> answer.answerNumber &
                ".answer-text" #> SHtml.text(answer.answer, updateAnswer(_, answer._id), "id" -> (answer._id + "e"))
            } &            
            "#add-answer" #> SHtml.ajaxSubmit("Add Answer", addNewAnswer())
          }
          case _ =>
            ".answer" #> ClearNodes &
            ".new-answer" #> ClearNodes &
            "#add-answer" #> ClearNodes
        }
      }

      "#question" #> SHtml.text(currentQuestion.map(_.question).openOr(""), questionText => currentQuestion = currentQuestion.map(q => q)) &
      "#questionType" #> "Question Type: %s".format(currentQuestion.map(_.questionType).openOr("")) &
      questionAnswers &
      "#reload-page [onclick]" #> SHtml.ajaxInvoke(reloadEditQuestion) & //TODO: drop the reload click
      "#confirm-edit" #> SHtml.ajaxSubmit("Save Changes", saveQuestion())
    })
  }

}
