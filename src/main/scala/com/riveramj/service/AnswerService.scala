package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._


object AnswerService extends Loggable {

  def createAnswer(number: Int, answer: String, questionId: ObjectId) = {
    val ans = Answer(
      _id = ObjectId.get(),
      answerNumber = number,
      answer = answer
    )

    saveAnswer(ans, questionId)
  }

  def saveAnswer(answer: Answer, questionId: ObjectId) = { //TODO: This is a cluster
    val question = QuestionService.getQuestionById(questionId) openOrThrowException "Bad Question"
    val updatedQuestion = question.copy(answers = question.answers :+ answer)
    val survey = SurveyService.getSurveyByQuestionId(question._id) openOrThrowException "no questionId"
    val updatedSurvey = survey.copy(questions =
        survey.questions.filter(_._id != questionId) :+ updatedQuestion
    )
    SurveyService.updateSurvey("questions._id" -> ("$oid" -> questionId.toString), updatedSurvey)

    getAnswerById(answer._id)
  }

  def getAnswerById(answerId: ObjectId) = {
    val survey = Survey.find("questions.answers._id" -> ("$oid" -> answerId.toString))
    val answer = survey.map { s =>
      s.questions.collect {
        case q: Question => q.answers
      }.flatten
    } getOrElse Nil

    val answers = answer.filter(_._id == answerId)
    answers.headOption
  }

  def findAnswerIdByResponse(answerChoice: String, questionId: ObjectId): Box[Answer] = {
    val answers = QuestionService.findAnswersByQuestionId(questionId)
    answers.filter(_.answerNumber == answerChoice.toInt).headOption
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
      case Full(QuestionType.choseOne) => {
        AnswerService.findAnswerIdByResponse(answerChoice, questionId) match {
          case Full(possibleAnswer) =>
            answerChoice
          case _ =>
            "-1"
        }
      }
      case Full(QuestionType.trueFalse) =>
        answerChoice.toLowerCase match {
          case "true" | "false" => answerChoice.toLowerCase
          case  _ => "-1"
        }
      case Full(QuestionType.ratingScale) =>
        answerChoice.toInt match {
          case 1|2|3|4|5 => answerChoice
          case  _ => "-1"
        }
      case Full(QuestionType.freeResponse) =>
        answerChoice.length match {
          case length if length > 3 => answerChoice
          case  _ => "-1"
        }
    }
  }

  def enumerateAnswers(question: Box[Question]) = {
    val answers = question.map( q => QuestionService.findAnswersByQuestionId(q._id)) openOr Nil
    answers.map { answer =>
      "%s: %s".format(answer.answerNumber, answer.answer)
    }.mkString(", ")
  }
}
