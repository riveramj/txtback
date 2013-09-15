package com.riveramj.util

import net.liftweb.common.Loggable
import com.riveramj.service.CompanyService.createCompany
import com.riveramj.service.SurveyorService.createSurveyor
import com.riveramj.service.SurveyService.createSurvey
import com.riveramj.service.QuestionService.createQuestion
import com.riveramj.service.SurveyInstanceService.createSurveyInstance
import com.riveramj.service.{SurveyService, QuestionService}
import com.riveramj.model.QuestionType

object  TestDataLoader extends Loggable {
  val surveyName = "questions about you"
  lazy val exampleSurveyId = SurveyService.getSurveyByName(surveyName) map(_._id)
  val company = createCompany("Company1")
  val newCompanyId = company.map(company => company._id).get

  def createTestUsers() {
    logger.info("Creating Test Data")

    createSurveyor(
      firstName = "Mike",
      lastName = "Rivera",
      email = "rivera.mj@gmail.com",
      companyId = newCompanyId,
      password = "password"
    )

    createSurveyor(
      firstName = "Calvin",
      lastName = "Leach",
      email = "cleach@magnetic-usa.com",
      companyId = newCompanyId,
      password = "password"
    )


  }
  def createTestQuestions() {

    val survey = createSurvey(
      name = surveyName,
      companyId = newCompanyId
    )

    val newSurveyId = survey.map(survey => survey._id).get

    val question1 = createQuestion(
      questionText = "what type of pet do you have?",
      questionType = QuestionType.choseOne,
      questionNumber = 1,
      surveyId = newSurveyId
    )
    val question1Id = question1.map(question => question._id).get

//    createAnswer(
//      parentQuestionId = question1Id,
//      answerNumber = 1,
//      answerText = "dog"
//    )
//    createAnswer(
//      parentQuestionId = question1Id,
//      answerNumber = 2,
//      answerText = "cat"
//    )
//    createAnswer(
//      parentQuestionId = question1Id,
//      answerNumber = 3,
//      answerText = "none"
//    )

    val question2 = createQuestion(
      questionText = "which state do you live in?",
      questionType = QuestionType.choseOne,
      questionNumber = 2,
      surveyId = newSurveyId
    )
    val question2Id = question2.map(question => question._id).get

//    createAnswer(
//      parentQuestionId = question2Id,
//      answerNumber = 1,
//      answerText = "georgia"
//    )
//    createAnswer(
//      parentQuestionId = question2Id,
//      answerNumber = 2,
//      answerText = "florida"
//    )
//    createAnswer(
//      parentQuestionId = question2Id,
//      answerNumber = 3,
//      answerText = "other"
//    )

    val question3 = createQuestion(
      questionText = "whats your favorite food",
      questionType = QuestionType.choseOne,
      questionNumber = 3,
      surveyId = newSurveyId
    )
    val question3Id = question3.map(question => question._id).get

//    createAnswer(
//      parentQuestionId = question3Id,
//      answerNumber = 1,
//      answerText = "pizza"
//    )
//    createAnswer(
//      parentQuestionId = question3Id,
//      answerNumber = 2,
//      answerText = "hot dogs"
//    )
//    createAnswer(
//      parentQuestionId = question3Id,
//      answerNumber = 3,
//      answerText = "other"
//    )

    val surveyInstance = createSurveyInstance(
      responderPhone = "4044090725",
      surveyId = newSurveyId,
      questionId = question1Id
    )

    val surveyInstanceId = surveyInstance.map(surveyInstance =>
      surveyInstance._id).get


  }
}
