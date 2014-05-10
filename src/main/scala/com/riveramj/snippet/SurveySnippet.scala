package com.riveramj.snippet

import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http._
import net.liftweb.http.js.{JE, JsCmds, JsCmd}
import net.liftweb.http.js.JsCmds.Noop

import org.bson.types.ObjectId

import com.riveramj.model.{QuestionType, Question}
import com.riveramj.service._
import com.riveramj.util.SecurityContext

object SurveySnippet {
  import com.riveramj.util.PathHelpers.loggedIn

  lazy val menu = Menu.param[String]("survey","survey",
    Full(_),
    (id) => id
  ) / "survey" / * >>
  loggedIn >>
  TemplateBox(() => Templates( "survey" :: Nil))

  object editQuestionIdRV extends RequestVar[Box[ObjectId]](Empty)
  object surveyIdRV extends RequestVar[Box[ObjectId]](Empty)
  object changedAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
  object newAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
  object deleteAnswersRV extends RequestVar[Box[Map[Long, String]]](Empty)
}

class SurveySnippet extends Loggable {
  import SurveySnippet._

  var newQuestion = ""
  var toPhoneNumber = ""
  var fromPhoneNumber = ""

  val surveyId = menu.currentValue map {ObjectId.massageToObjectId(_)} openOrThrowException "Not valid survey id"
  surveyIdRV(Full(surveyId))
  surveyIdRV.is

  def deleteQuestion(questionId: ObjectId):JsCmd = {
    QuestionService.deleteQuestionById(questionId) match {
      case Empty =>
        JsCmds.Run("$('#" + questionId + "').parent().remove()")
      case _ => logger.error("couldn't delete survey with id %s" format questionId)
        //TODO: provide feedback on delete action
    }
  }

  def createQuestion(surveyId: ObjectId, questionType: QuestionType): JsCmd = {
    if(newQuestion.nonEmpty) {
      QuestionService.createQuestion(QuestionService.nextQuestionNumber(surveyId), newQuestion, questionType, surveyId)
      S.notice("survey-question-created", "Question Created")
    }
    else
      S.error("survey-question-created", "Question is required")
  }

  def questionAndAnswers(question: Question, surveyIsEditable: Boolean): CssSel = {
    val questionId = question._id
    val answers = QuestionService.findAnswersByQuestionId(questionId)

    def editQuestionCssSel = {
      if (surveyIsEditable) {
        ".edit-question [onclick]" #> SHtml.ajaxInvoke(() => {
          editQuestionIdRV(Full(questionId))
          editQuestionIdRV.is
          Noop
        }) 
      } else {
        ".edit-question" #> ClearNodes
      }
    }

    def deleteQuestionCssSel = {
      if (surveyIsEditable) {
        ".delete-question [onclick]" #> SHtml.ajaxInvoke(() => {
          JsCmds.Confirm("Are you sure you want to delete the question?", {
            SHtml.ajaxInvoke(() => {
              deleteQuestion(questionId)
            }).cmd
          })
        })
      } else {
        ".delete-question" #> ClearNodes
      }
    }

    ".question *" #> question.question &
    ".question [id]" #> questionId.toString &
    editQuestionCssSel &
    deleteQuestionCssSel &
    ".answer" #> answers.map{ answer =>
      ".answer-number *" #> answer.answerNumber &
      ".answer-text *" #> answer.answer &
      ".answer-text [id]" #> answer._id.toString
    }
  }

  def startSurvey:JsCmd = {
    val phoneNumbers = toPhoneNumber.split("""[, ;]""")

    val firstQuestion = SurveyService.getFirstQuestionBySurveyId(surveyId) openOrThrowException "No first question"
    
    phoneNumbers.foreach { number => 
      val toPhoneNumber = PhoneNumberService.stripNonNumeric(number)
      SurveyInstanceService.createSurveyInstance(
        responderPhone = toPhoneNumber,
        surveyId = surveyId,
        currentQuestionId = firstQuestion._id,
        senderPhoneNumber = fromPhoneNumber
      )
      
      TwilioService.sendMessage(
        toPhoneNumber,
        QuestionService.questionToSend(Full(firstQuestion))
      )
    }

    SurveyService.markSurveyAsStarted(surveyId)

    S.notice("send-survey-notice", "Survey Sent") //TODO: validate it actually sent
  }

  def renderQuestions() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val surveyIsEditable = survey.map(_.startedDate.isEmpty).openOr(true)
    val questions = SurveyService.getAllQuestionsBySurveyId(surveyId)

    editQuestionIdRV(SurveyService.getFirstQuestionBySurveyId(surveyId).map(_._id))
    editQuestionIdRV.is

    ClearClearable andThen
    "#survey-name *" #> survey.map(_.name) &
    "#view-responses [href]" #> ("/survey/" + surveyId + "/responses") &
    "#question-list" #> questions.map{ question =>
      questionAndAnswers(question, surveyIsEditable)
    } 
  }

  def sendSurvey() = {
    val userPhoneNumbers = SecurityContext.currentUser.map(_.phoneNumbers) openOr Nil

    val phoneNumberSelect = SHtml.select(
        userPhoneNumbers.map(number => (number.number, number.number)),
        Empty,
        fromPhoneNumber = _
      )
    
    "#phone-select" #> phoneNumberSelect &
    "#phone-number" #> SHtml.ajaxTextarea(toPhoneNumber, toPhoneNumber = _) &
    "#send-survey [onclick]" #> SHtml.ajaxInvoke(startSurvey _)
  }

  def createNewQuestion() = {
    val survey = SurveyService.getSurveyById(surveyId)
    val surveyIsEditable = survey.map(_.startedDate.isEmpty).openOr(true)
    if (surveyIsEditable) {
      "#new-question" #> SHtml.text(newQuestion, newQuestion = _) &
      "#chose-one" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, QuestionType.choseOne)) &
      "#true-false" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, QuestionType.trueFalse)) &
      "#rating-scale" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, QuestionType.ratingScale)) &
      "#free-response" #> SHtml.onSubmitUnit(() => createQuestion(surveyId, QuestionType.freeResponse))
    } else {
      ".input-group" #> ClearNodes
    }
  }
}
