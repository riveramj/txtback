package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.common.Full


object AnswerServiceService extends Loggable {

  def createAnswer(surveyAnswers:List[String], parentQuestionId:Long) = {
    surveyAnswers.foreach{ surveyAnswer =>
      val answer = Answer.create
        .answer(surveyAnswer)
        .questionId(parentQuestionId)
        .answerId(generateLongId())

      tryo(saveAnswer(answer)) flatMap {
        u => u match {
          case Full(newAnswer:Answer) => Full(newAnswer)
          case (failure: Failure) => failure
          case _ => Failure("Unknown error")
        }
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
    Answer.findAll(By(Answer.questionId, questionId))
  }

  def getAllAnswers = {
    Answer.findAll()
  }

}
