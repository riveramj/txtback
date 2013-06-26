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
      surveyAnswers = List("Answer 1","Answer 2","Answer 3","Answer 4")
    )

    val question2 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 2",
      questionNumber = QuestionService.nextQuestionNumber(newSurveyId)
    )
    val question2Id = question2.map(question => question.questionId.get) openOr 0L

    createAnswer(
      parentQuestionId = question2Id,
      surveyAnswers = List("Answer 11","Answer 22","Answer 33","Answer 44")
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
      surveyAnswers = List("Answer 111","Answer 222","Answer 333","Answer 444")
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
