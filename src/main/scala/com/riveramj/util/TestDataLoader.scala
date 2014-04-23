package com.riveramj.util

import net.liftweb.common.Loggable
import net.liftweb.util.Props

import com.riveramj.service.SurveyorService.createSurveyor
import com.riveramj.service.SurveyService.createSurvey
import com.riveramj.service.QuestionService.createQuestion
import com.riveramj.service.SurveyInstanceService._
import com.riveramj.service._
import com.riveramj.model.{QuestionType, PhoneNumber}

object  TestDataLoader extends Loggable {
  val surveyName = "questions about you"
  lazy val exampleSurveyId = SurveyService.getSurveyByName(surveyName) map(_._id)

  def createTestUsers() {
    logger.info("Creating Test Data")

    val testPhoneNumber = Props.get("test.phone.number").openOr("")
    val formattedTestNumber = PhoneNumberService.longFormatPhoneNumber(testPhoneNumber)

    createSurveyor(
      firstName = "Mike",
      lastName = "Rivera",
      email = "rivera.mj@gmail.com",
      password = "password", 
      phoneNumber = PhoneNumber(number = formattedTestNumber, sid = "")
    )

    createSurveyor(
      firstName = "Josh",
      lastName = "Erickson",
      email = "josh@applecrumbs.com",
      password = "password", 
      phoneNumber = PhoneNumber(number = formattedTestNumber, sid = "")
    )

  }
  def createTestQuestions() {
    val userIdMike = SurveyorService.getUserByEmail("rivera.mj@gmail.com").map(_._id) openOrThrowException "no valid user"

    val userIdJosh = SurveyorService.getUserByEmail("josh@applecrumbs.com").map(_._id) openOrThrowException "no valid user"

    val testUsers = List(userIdMike, userIdJosh)

    val surveyIds = testUsers.map { userId => 
      createSurvey(
        name = surveyName,
        userId = userId
      ).openOrThrowException("Didnt get survey")
    }

    surveyIds.map { survey => 

      val newSurveyId = survey._id
      
      val question1Id = createQuestion(
        questionText = "what type of pet do you have?",
        questionType = QuestionType.choseOne,
        questionNumber = 1,
        surveyId = newSurveyId
      ).map(_._id).openOrThrowException("Didnt get question 1 Id")

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

      val question2Id = createQuestion(
        questionText = "which state do you live in?",
        questionType = QuestionType.choseOne,
        questionNumber = 2,
        surveyId = newSurveyId
      ).map(_._id).openOrThrowException("Didnt get question 2 Id")

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

      val question3Id = createQuestion(
        questionText = "whats your favorite food",
        questionType = QuestionType.choseOne,
        questionNumber = 3,
        surveyId = newSurveyId
      ).map(_._id).openOrThrowException("Didnt get question 3 Id")

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
    }
  }
}
