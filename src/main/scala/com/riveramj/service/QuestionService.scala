package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model._
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.mapper.By
import net.liftweb.common.Full


object QuestionService extends Loggable {

  def createQuestion(surveyQuestion:String, parentSurveyId:Long) = {
    val question = Question.create
      .question(surveyQuestion)
      .surveyId(parentSurveyId)
      .questionId(generateStringId)

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

  def getQuestionById(questionId: String): Box[Question] = {
    Question.find(By(Question.questionId, questionId))
  }

  def deleteQuestionById(questionId: String): Box[Boolean] = {
    val question = Question.find(By(Question.questionId, questionId))
    question.map(_.delete_!)
  }

  def getAllQuestions = {
    Question.findAll()
  }

}
