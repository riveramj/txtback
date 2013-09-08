package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import net.liftweb.mapper.By
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._


object AnswerService extends Loggable {

  def findAllAnswersByQuestionId(questionId: ObjectId): List[String] = {
    val responses = SurveyInstance.findAll("responses._id" -> questionId)
    val answers = responses.flatMap(_.responses.map(_.answer))
    answers
  }

  def getAllAnswers = {
    val surveyInstances = SurveyInstanceService.getAllSurveyInstances
    surveyInstances.flatMap(_.responses.map(_.answer))
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
