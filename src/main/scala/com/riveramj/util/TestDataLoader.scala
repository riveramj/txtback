package com.riveramj.util

import net.liftweb.common.Loggable
import bootstrap.liftweb.Boot
import com.riveramj.service.CompanyService.createCompany
import com.riveramj.service.SurveyorService.createSurveyor
import com.riveramj.service.SurveyService.createSurvey
import com.riveramj.service.QuestionService.createQuestion

object  TestDataLoader extends Loggable {
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

    val question2 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 2"
    )
    val question2Id = question2.map(question => question.questionId.get) openOr 0L

    val question3 = createQuestion(
      parentSurveyId = newSurveyId,
      surveyQuestion = "This is question 3"
    )
    val question3Id = question3.map(question => question.questionId.get) openOr 0L
  }
}
