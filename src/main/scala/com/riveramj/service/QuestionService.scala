package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.mapper.By
import com.riveramj.service.AnswerService._


object QuestionService extends Loggable {

  def createQuestion(questionNumber:Long ,surveyQuestion:String, questionType: String, parentSurveyId:Long) = {
    val question = Question.create
      .question(surveyQuestion)
      .questionType(questionType)
      .surveyId(parentSurveyId)
      .questionId(generateLongId())
      .questionNumber(questionNumber)

    tryo(saveQuestion(question)) flatMap {
      u => u match {
        case Full(newQuestion:Question) => Full(newQuestion)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def saveQuestion(question:Question):Box[Question] = {

    val uniqueConstraintPattern = """.*Unique(.+)""".r
    val validateErrors = question.validate

    if (validateErrors.isEmpty) {
      tryo(question.saveMe()) match {
        case Full(newQuestion:Question) => Full(newQuestion)
        case Failure(_, Full(err), _) => {
          val error = err.getMessage.substring(0, err.getMessage.indexOf("\n"))
          error match {
            case uniqueConstraintPattern(x) => Failure("Question Already Exists")
            case _ => Failure("Unknown error")
          }
        }
        case _ => Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getQuestionById(questionId: Long): Box[Question] = {
    Question.find(By(Question.questionId, questionId))
  }

  def deleteQuestionById(questionId: Long): Box[Boolean] = {
    val question = Question.find(By(Question.questionId, questionId))
    val answers = AnswerService.findAllAnswersByQuestionId(questionId)
    answers.map(_.delete_!)
    question.map(_.delete_!)
  }

  def findAllSurveyQuestions(surveyId:Long): List[Question] = {
    Question.findAll(By(Question.surveyId, surveyId))
  }

  def getAllQuestions = {
    Question.findAll()
  }

  def nextQuestionNumber(surveyId: Long) = {
    QuestionService.findAllSurveyQuestions(surveyId).length + 1
  }

  def getFirstQuestion(surveyId: Long) = {
    Question.find(By(Question.surveyId,surveyId), By(Question.questionNumber,1))
  }

  def findQuestionByNumber(questionNumber: Long) = {
    Question.find(By(Question.questionNumber,questionNumber))
  }

  def questionToSend(question:Box[Question]) = {
    val questionText = question.map(_.question.get).openOr("")
    question.map(_.questionType.get) match {
      case Full("choseOne") =>
        "%s Respond: %s".format(questionText, enumerateAnswers(question))
      case Full("trueFalse") =>
        "True or False: %s".format(questionText)
      case Full("freeResponse") =>
        "Respond to the following: %s".format(questionText)
      case Full("ratingScale") =>
        "Rate the following on a 1 (disagree) - 5 (agree) scale: %s".format(questionText)
    }
  }
}
