package code.service

import org.specs2.mutable.Specification
import bootstrap.liftweb.Boot
import com.riveramj.service.SurveyService._
import net.liftweb.common.Full

/**
 * Created with IntelliJ IDEA.
 * Surveyor: mjr
 * Date: 4/22/13
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */
class SurveyServiceSpecs extends Specification {

  step {
    val boot = new Boot
    boot.boot
  }

  "the Survey Service" should {
    args(sequential=true)

    "create survey" in {
      val survey = createSurvey("Mike Survey")
      survey must not beEmpty
    }
    "find survey by name" in {
      val survey = getSurveyByName("Mike Survey")
      survey must not beEmpty

      survey.map(_.surveyName.get).openOr("") must beEqualTo("Mike Survey")
    }
    "find survey by id" in {
      val survey = getSurveyByName("Mike Survey")
      survey must not beEmpty

      val surveyName = survey.map(_.surveyName.get)
      surveyName must beEqualTo(Full("Mike Survey"))

      val surveyId = survey.map(_.surveyId.get).getOrElse("")

      val surveyById = getSurveyById(surveyId)
      surveyById.map(_.surveyName.get).getOrElse("") must beEqualTo("Mike Survey")
    }

    "find all surveys" in {
      val companies = getAllSurveys
      companies must not beEmpty
    }

    "delete survey by id" in {

      val survey = getSurveyByName("Mike Survey")
      survey must not beEmpty

      val surveyName = survey.map(_.surveyName.get)
      surveyName must beEqualTo(Full("Mike Survey"))

      val surveyId = survey.map(_.surveyId.get).getOrElse("")

      deleteSurveyById(surveyId).openOr(false) must beTrue
    }
  }
  
}
