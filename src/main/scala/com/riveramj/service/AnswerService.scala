package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.common.Full


object AnswerService extends Loggable {

  def createAnswer(answerNumber: Int, answerText: String, parentQuestionId:Long) = {
    val answer = Answer.create
      .answer(answerText)
      .answerNumber(answerNumber)
      .QuestionId(parentQuestionId)
      .answerId(generateLongId())

    tryo(saveAnswer(answer)) flatMap {
      u => u match {
        case Full(newAnswer:Answer) => Full(newAnswer)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")

      }
    }
  }

  def saveAnswer(answer:Answer):Box[Answer] = {

    val uniqueConstraintPattern = """.*Unique(.+)""".r
    val validateErrors = answer.validate

    if (validateErrors.isEmpty) {
      tryo(answer.saveMe()) match {
        case Full(newAnswer:Answer) => Full(newAnswer)
        case Failure(_, Full(err), _) => {
          val error = err.getMessage.substring(0, err.getMessage.indexOf("\n"))
          error match {
            case uniqueConstraintPattern(x) => Failure("Answer Already Exists")
            case _ => Failure("Unknown error")
          }
        }
        case _ => Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getAnswerById(answerId: Long): Box[Answer] = {
    Answer.find(By(Answer.answerId, answerId))
  }

  def deleteAnswerById(answerId: Long): Box[Boolean] = {
    val answer = Answer.find(By(Answer.answerId, answerId))
    answer.map(_.delete_!)
  }

  def findAllAnswersByQuestionId(questionId: Long): List[Answer] = {
    Answer.findAll(By(Answer.QuestionId, questionId))
  }

  def getAllAnswers = {
    Answer.findAll()
  }

  def findAnswerIdByResponse(answerChoice: String, questionId: Long) = {
    Answer.find(By(Answer.QuestionId, questionId), By(Answer.answerNumber,answerChoice.toInt))
  }

  def lookupAnswerChoice(answerChoice: String, questionId: Long) = {
    val answer = AnswerService.findAnswerIdByResponse(answerChoice, questionId)
    answer match {
      case Full(possibleAnswer) =>
        possibleAnswer.answerId.get
      case Empty =>
        -1L
    }
  }

  def findNextAnswerNumber(questionId: Long) = {
    val answers = Answer.findAll(By(Answer.QuestionId, questionId))

    answers.map(_.answerNumber.get).max + 1
  }

  def changeAnswer(newAnswer: String, answerId: Long) {
    val answer = AnswerService.getAnswerById(answerId).openOrThrowException("Couldn't Find Answer")
    AnswerService.saveAnswer(answer.answer(newAnswer))
  }
}
