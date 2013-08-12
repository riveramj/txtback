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
    Answer.find(By(Answer.QuestionId, questionId), By(Answer.answerNumber,answerChoice.toInt)) //TODO: This will blow up on non-int String
  }

  def recordAnswer(answerChoice: String, questionId: Long) = {
    val question = QuestionService.getQuestionById(questionId)
    question.map(_.questionType.get) match {
      case Full("choseOne") => {
        AnswerService.findAnswerIdByResponse(answerChoice, questionId) match {
          case Full(possibleAnswer) =>
            answerChoice
          case Empty =>
            "-1"
        }
      }
      case Full("trueFalse") =>
        answerChoice.toLowerCase match {
          case "true" | "false" => answerChoice.toLowerCase
          case  _ => "-1"
        }
      case Full("ratingScale") =>
        answerChoice.toInt match {
          case 1|2|3|4|5 => answerChoice
          case  _ => "-1"
        }
      case Full("freeResponse") =>
        answerChoice.length match {
          case length if length > 3 => answerChoice
          case  _ => "-1"
        }
    }
  }

  def findNextAnswerNumber(questionId: Long) = {
    val answers = Answer.findAll(By(Answer.QuestionId, questionId))

    answers.map(_.answerNumber.get) match {
      case answerList if answerList.length > 0 => answerList.max + 1
      case _ => 1
    }
  }

  def changeAnswer(newAnswer: String, answerId: Long) {
    val answer = AnswerService.getAnswerById(answerId).openOrThrowException("Couldn't Find Answer")
    AnswerService.saveAnswer(answer.answer(newAnswer))
  }

  def enumerateAnswers(question: Box[Question]) = {
    val answers = findAllAnswersByQuestionId(question.map(_.questionId.get).openOr(0L))
    answers.map { answer =>
      "%s: %s".format(answer.answerNumber.get, answer.answer.get)
    }.mkString(", ")
  }
}
