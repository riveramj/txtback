package com.riveramj.util

import net.liftweb.common.Loggable
import bootstrap.liftweb.Boot
import com.riveramj.service.CompanyService.createCompany
import com.riveramj.service.UserService.createSurveyor
import com.riveramj.service.SurveyService.createSurvey
import com.riveramj.service.QuestionService.createQuestion
import com.riveramj.service.AnswerService.createAnswer

object TestDataLoader extends Loggable {

  def initialize = {
    val boot = new Boot
    boot.boot
  }



  def main(args: Array[String]) {
    initialize
    val testData = new TestDataLoader
    testData.createTestData()
  }
}

class  TestDataLoader extends Loggable {
  def createTestData() {
    val company = createCompany("Company1")

    val newCompanyId = company.map(company => company.companyId.get) openOr 0L
    println(newCompanyId  + " is the company id")

    createSurveyor(
      firstName = "Mike",
      lastName = "Rivera",
      email = "rivera.mj@gmail.com",
      companyId = newCompanyId,
      password = "password"
    )

    val survey = createSurvey(
      name = "Survey 1",
      companyId = newCompanyId
    )

    val newSurveyId = survey.map(survey => survey.surveyId.get) openOr 0L

    val question1 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 1"
    )
    val question1Id = question1.map(question => question.questionId.get) openOr 0L

    createAnswer(
      parentQuestionId = question1Id,
      surveyAnswers = List("Answer 1","Answer 2","Answer 3","Answer 4")
    )

    val question2 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 2"
    )
    val question2Id = question2.map(question => question.questionId.get) openOr 0L

    createAnswer(
      parentQuestionId = question2Id,
      surveyAnswers = List("Answer 11","Answer 22","Answer 33","Answer 44")
    )

    val question3 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 3"
    )
    val question3Id = question3.map(question => question.questionId.get) openOr 0L

    createAnswer(
      parentQuestionId = question3Id,
      surveyAnswers = List("Answer 111","Answer 222","Answer 333","Answer 444")
    )
  }
}
