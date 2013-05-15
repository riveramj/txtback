package com.riveramj.snippet

import com.riveramj.service.{AnswerService, SurveyService, QuestionService}

import net.liftweb.util.ClearClearable
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import net.liftweb.common.Full
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http.Templates
import com.riveramj.util.Paths
import com.riveramj.model.Question


object SurveySnippet {
  lazy val menu = Menu.param[String]("survey","survey",
    Full(_),
    (id) => id
  ) / "survey" / * >>
  TemplateBox(() => Templates( "survey" :: Nil))
}

class SurveySnippet {
  import SurveySnippet._

  def questionAndAnswers(question: Question) = {
    val answers = AnswerService.findAllAnswersByQuestionId(question.questionId.get)
    ".question *" #> question.question.get &
    "#answer-list" #> answers.map{ answer =>
      ".answer *" #> answer.answer.get
    }

  }

  def render() = {

    val surveyId = menu.currentValue map {_.toLong} openOr 0L

    val survey = SurveyService.getSurveyById(surveyId)
    val questions = QuestionService.findAllSurveyQuestions(surveyId)

    ClearClearable andThen
      "#survey-name *" #> survey.map(_.surveyName.get) &
      "#question-list" #> questions.map{ question =>
          questionAndAnswers(question)
        }
  }
}
