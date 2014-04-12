package com.riveramj.util

import net.liftweb.common.Loggable
import com.riveramj.service.SurveyorService.createSurveyor
import com.riveramj.service.SurveyService.createSurvey
import com.riveramj.service.QuestionService.createQuestion
import com.riveramj.service.SurveyInstanceService._
import com.riveramj.service._
import com.riveramj.model.QuestionType

object  TestDataLoader extends Loggable {
  val surveyName = "questions about you"
  lazy val exampleSurveyId = SurveyService.getSurveyByName(surveyName) map(_._id)

  def createTestUsers() {
    logger.info("Creating Test Data")

    createSurveyor(
      firstName = "Mike",
      lastName = "Rivera",
      email = "rivera.mj@gmail.com",
      password = "password", 
      phoneNumber = "15005550006"
    )

  }
  def createTestQuestions() {
    val userId = SurveyorService.getUserByEmail("rivera.mj@gmail.com").map(_._id) openOrThrowException "no valid user"

    val survey = createSurvey(
      name = surveyName,
      userId = userId
    )

    val newSurveyId = survey.map(survey => survey._id).openOrThrowException("Didnt get survey Id")

    val question1 = createQuestion(
      questionText = "what type of pet do you have?",
      questionType = QuestionType.choseOne,
      questionNumber = 1,
      surveyId = newSurveyId
    )

    val question1Id = question1.map(question => question._id).openOrThrowException("Didnt get question 1 Id")

    AnswerService.createAnswer(
      questionId = question1Id,
      number = 1,
      answer = "dog"
    )
    AnswerService.createAnswer(
      questionId = question1Id,
      number = 2,
      answer = "cat"
    )
    AnswerService.createAnswer(
      questionId = question1Id,
      number = 3,
      answer = "none"
    )

    val question2 = createQuestion(
      questionText = "which state do you live in?",
      questionType = QuestionType.choseOne,
      questionNumber = 2,
      surveyId = newSurveyId
    )
    val question2Id = question2.map(question => question._id).openOrThrowException("Didnt get question 2 Id")

    AnswerService.createAnswer(
      questionId = question2Id,
      number = 1,
      answer = "georgia"
    )
    AnswerService.createAnswer(
      questionId = question2Id,
      number = 2,
      answer = "florida"
    )
    AnswerService.createAnswer(
      questionId = question2Id,
      number = 3,
      answer = "other"
    )

    val question3 = createQuestion(
      questionText = "whats your favorite food",
      questionType = QuestionType.choseOne,
      questionNumber = 3,
      surveyId = newSurveyId
    )
    val question3Id = question3.map(question => question._id).openOrThrowException("Didnt get question 3 Id")

    AnswerService.createAnswer(
      questionId = question3Id,
      number = 1,
      answer = "pizza"
    )
    AnswerService.createAnswer(
      questionId = question3Id,
      number = 2,
      answer = "hot dogs"
    )
    AnswerService.createAnswer(
      questionId = question3Id,
      number = 3,
      answer = "other"
    )

    val surveyInstance = createSurveyInstance(
      responderPhone = "4044090725",
      surveyId = newSurveyId,
      currentQuestionId = question1Id,
      senderPhoneNumber = "7702123225"
    )

    val surveyInstanceId = surveyInstance.map(surveyInstance =>
      surveyInstance._id).openOrThrowException("Didnt get survey Instance Id")

    deleteSurveyInstanceById(surveyInstanceId)
  }
}
