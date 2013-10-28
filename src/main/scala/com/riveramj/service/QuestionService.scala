package com.riveramj.service

import net.liftweb.common._
import com.riveramj.service.AnswerService._
import com.riveramj.model._
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
    val survey = SurveyService.getSurveyById(surveyId) openOrThrowException "Bad Survey"
    val updatedSurvey = survey.copy(questions = survey.questions.filter(_._id != question._id):+ question)
    SurveyService.updateSurvey(updatedSurvey)
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
    getQuestionById(questionId)
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

  def findAnswersByQuestionId(questionId: ObjectId): Seq[Answer] = {
    getQuestionById(questionId) map(_.answers.sortBy(_.answerNumber)) getOrElse Nil
  }

  def findNextQuestion(currentQuestionId: ObjectId, surveyId: ObjectId): Box[Question] = {
    val currentQuestion = QuestionService.getQuestionById(currentQuestionId) openOrThrowException "Bad question"
    val questionNumber = currentQuestion.questionNumber + 1
    val survey = SurveyService.getAllQuestionsBySurveyId(surveyId)     
    println(    survey.filter{_.questionNumber == questionNumber}.headOption + "output2")
    survey.filter{_.questionNumber == questionNumber}.headOption
  }

  def updateCurrentNextQuestionId(surveyInstance: SurveyInstance): SurveyInstance = {
   val surveyId = surveyInstance.surveyId
   println(surveyId + " surveyId") 
   val currentQuestionId = surveyInstance.currentQuestionId.get  
   println(currentQuestionId + " currentquestionId")
   val nextQuestionId = findNextQuestion(currentQuestionId, surveyId).map(_._id)
   println(nextQuestionId + " nextQuestionId")
   println(surveyInstance.copy(
     currentQuestionId = surveyInstance.nextQuestionId, 
     nextQuestionId = nextQuestionId
   ) 
 + " output")
   surveyInstance.copy(
     currentQuestionId = surveyInstance.nextQuestionId, 
     nextQuestionId = nextQuestionId
   ) 
 }
}
