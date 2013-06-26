package com.riveramj.util

import net.liftweb.common.Loggable
import com.riveramj.service.CompanyService.createCompany
import com.riveramj.service.SurveyorService.createSurveyor
import com.riveramj.service.SurveyService.createSurvey
import com.riveramj.service.QuestionService.createQuestion
import com.riveramj.service.SurveyInstanceService.createSurveyInstance
import com.riveramj.service.QASetService.createQASet
import com.riveramj.service.{AnswerService, SurveyService, QuestionService}
import com.riveramj.service.AnswerService.createAnswer

object  TestDataLoader extends Loggable {
  val surveyName = "Survey 1"
  lazy val exampleSurveyId = SurveyService.getSurveyByName(surveyName) map(_.surveyId.get) getOrElse 0L

  def createTestData() {
    logger.info("Creating Test Data")

    val company = createCompany("Company1")

    val newCompanyId = company.map(company => company.companyId.get) openOr 0L

    createSurveyor(
      firstName = "Mike",
      lastName = "Rivera",
      email = "rivera.mj@gmail.com",
      companyId = newCompanyId,
      password = "password"
    )

    val survey = createSurvey(
      name = surveyName,
      companyId = newCompanyId
    )

    val newSurveyId = survey.map(survey => survey.surveyId.get) openOr 0L

    val question1 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 1",
      questionNumber = QuestionService.nextQuestionNumber(newSurveyId)
    )
    val question1Id = question1.map(question => question.questionId.get) openOr 0L

    createAnswer(
      parentQuestionId = question1Id,
      answerNumber = 1,
      answerText = "answer 1"
    )
    createAnswer(
      parentQuestionId = question1Id,
      answerNumber = 2,
      answerText = "answer 2"
    )
    createAnswer(
      parentQuestionId = question1Id,
      answerNumber = 3,
      answerText = "answer 3"
    )

    val question2 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 2",
      questionNumber = QuestionService.nextQuestionNumber(newSurveyId)
    )
    val question2Id = question2.map(question => question.questionId.get) openOr 0L

    createAnswer(
      parentQuestionId = question2Id,
      answerNumber = 1,
      answerText = "answer 11"
    )
    createAnswer(
      parentQuestionId = question2Id,
      answerNumber = 2,
      answerText = "answer 22"
    )
    createAnswer(
      parentQuestionId = question2Id,
      answerNumber = 3,
      answerText = "answer 33"
    )

    val question3 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 3",
      questionNumber = QuestionService.nextQuestionNumber(newSurveyId)
    )
    val question3Id = question3.map(question => question.questionId.get) openOr 0L

    val surveyInstance = createSurveyInstance(
      responderPhone = "4044090725",
      surveyId = newSurveyId,
      questionId = question1Id
    )

    val surveyInstanceId = surveyInstance.map(surveyInstance =>
      surveyInstance.surveyInstanceId.get) openOr 0L

    createAnswer(
      parentQuestionId = question3Id,
      answerNumber = 1,
      answerText = "answer 111"
    )
    createAnswer(
      parentQuestionId = question3Id,
      answerNumber = 2,
      answerText = "answer 222"
    )
    createAnswer(
      parentQuestionId = question3Id,
      answerNumber = 3,
      answerText = "answer 333"
    )

    createQASet(
      surveyInstanceId = surveyInstanceId,
      questionId = question1Id,
      answerId = AnswerService.lookupAnswerChoice("1", question1Id)
    )

    createQASet(
      surveyInstanceId = surveyInstanceId,
      questionId = question2Id,
      answerId = AnswerService.lookupAnswerChoice("1", question2Id)
    )

    createQASet(
      surveyInstanceId = surveyInstanceId,
      questionId = question3Id,
      answerId = AnswerService.lookupAnswerChoice("2", question3Id)
    )
  }
}
