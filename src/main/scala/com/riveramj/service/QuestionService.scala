package com.riveramj.service

import net.liftweb.common._
import com.riveramj.service.AnswerService._
import com.riveramj.model.{Answer, Survey, Question, QuestionType}
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import com.mongodb.{BasicDBObject, DBObject}

object QuestionService extends Loggable {

  def createQuestion(questionNumber: Int, questionText: String, questionType: QuestionType, surveyId: ObjectId) = {
    val question = Question(
      _id = ObjectId.get,
      question = questionText,
      questionType = questionType,
      questionNumber = questionNumber,
      answers = Nil
    )

    saveQuestion(question, surveyId)
  }

  def saveQuestion(question: Question, surveyId: ObjectId): Box[Question] = {
    val survey = SurveyService.getSurveyById(surveyId)
    val existingQuestions = survey.map(_.questions) openOr Nil
    survey.map(_.copy(questions = existingQuestions :+ question).save)
    getQuestionById(question._id)
  }

  def getQuestionById(questionId: ObjectId): Box[Question] = {
    val survey = SurveyService.getSurveyByQuestionId(questionId)
    val question  = survey.map(_.questions.filter(_._id == questionId)) getOrElse Nil
    question.headOption
  }

  def deleteQuestionById(questionId: ObjectId) = {
    getQuestionById(questionId)
    val survey = SurveyService.getSurveyByQuestionId(questionId) openOrThrowException "No Valid Survey"
    val filteredQuestions = survey.questions.filter(_._id != questionId)
    SurveyService.updateSurvey(
      "questions._id" -> ("$oid" -> questionId.toString),
      survey.copy(questions = filteredQuestions)
    )
  }

  def getAllQuestions: List[Question] = {
    Survey.findAll.flatMap(_.questions)
  }

  def nextQuestionNumber(surveyId: ObjectId) = {
    SurveyService.getAllQuestionsBySurveyId(surveyId).length + 1
  }

//  def findQuestionByNumber(questionNumber: Long) = {
//    Question.find(By(Question.questionNumber,questionNumber))
//  }

  def questionToSend(question:Box[Question]) = {
    val questionText = question.map(_.question).openOr("")
    question.map(_.questionType) match {
      case Full(QuestionType.choseOne) =>
        "%s Respond: %s".format(questionText, enumerateAnswers(question))
      case Full(QuestionType.trueFalse) =>
        "True or False: %s".format(questionText)
      case Full(QuestionType.freeResponse) =>
        "Respond to the following: %s".format(questionText)
      case Full(QuestionType.ratingScale) =>
        "Rate the following on a 1 (disagree) - 5 (agree) scale: %s".format(questionText)
    }
  }

  def findAnswersByQuestionId(questionId: ObjectId): List[Answer] = {
    getQuestionById(questionId) map(_.answers) getOrElse Nil
  }
}
