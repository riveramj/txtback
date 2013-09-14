package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._


object AnswerService extends Loggable {

  def findAnswerIdByResponse(answerChoice: String, questionId: ObjectId) = {
    val answers = QuestionService.findAnswersByQuestionId(questionId)
    answers.filter(_.answerNumber == answerChoice)
  }

//  def findAllAnswersByQuestionId(questionId: ObjectId): List[Answer] = {
//    val responses = SurveyInstance.findAll("responses._id" -> questionId)
//    val answers = responses.flatMap(_.responses.map(_.answer))
//    answers
//  }

  def getAllAnswers = {
    val surveyInstances = SurveyInstanceService.getAllSurveyInstances
    surveyInstances.flatMap(_.responses.map(_.answer))
  }

  def findNextAnswerNumber(questionId: ObjectId) = {
    val answers = QuestionService.findAnswersByQuestionId(questionId)

    answers.map(_.answerNumber) match {
      case answerList if answerList.length > 0 => answerList.max + 1
      case _ => 1
    }
  }

  def verifyAnswer(answerChoice: String, questionId: ObjectId) = {
    val question = QuestionService.getQuestionById(questionId)
    question.map(_.questionType) match {
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
}
