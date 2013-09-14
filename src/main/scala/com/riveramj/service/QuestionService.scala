package com.riveramj.service

import net.liftweb.common._
import net.liftweb.mapper.By
import com.riveramj.service.AnswerService._
import com.riveramj.model.{Survey, Question, QuestionType}
import org.bson.types.ObjectId


object QuestionService extends Loggable {

  def createQuestion(questionNumber: Int, question: String, questionType: QuestionType, surveyId: ObjectId) = {
    val question = Question(
      _id = ObjectId.get,
      question = question,
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
    val surveys = Survey.findAll

    {
      for {
        survey <- surveys
        question <- survey.questions
          if question._id == questionId
      }
      yield
        Full(question)
    }.head
  }

//  def deleteQuestionById(questionId: ObjectId) = {
//    getQuestionById(questionId).map
//  }

  def getAllQuestions: List[Question] = {
    Survey.findAll.flatMap(_.questions)
  }

//  def nextQuestionNumber(surveyId: Long) = {
//    QuestionService.findAllSurveyQuestions(surveyId).length + 1
//  }

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
}
