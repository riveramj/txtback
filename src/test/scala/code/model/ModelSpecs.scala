package code.model

import org.specs2.mutable.Specification
import bootstrap.liftweb.Boot
import net.liftweb.mapper.By
import com.riveramj.model._

class ModelSpecs extends Specification {

  step {
    val boot = new Boot
    boot.boot
  }

  "the Company mapper" should {
    args(sequential=true)

    "create test company" in {
      val company = Company.create
      company.companyName("company name")
      company.companyId(100000)
      company.save must beTrue
      company.id.get must not equalTo -1L
    }
    "find a company by name" in {
      val company = Company.find(By(Company.companyName, "company name"))
      company must not beEmpty

      val name = company.map(_.companyName) openOr ""
      name must beEqualTo("company name")
    }
    "delete test company" in {
      val company = Company.find(By(Company.companyName, "company name"))
      company must not beEmpty

      company.map(_.delete_!).openOr(false) must beTrue
    }
  }

  "the Users mapper" should {
    args(sequential=true)

    "create test user" in {
      val user = Users.create
      user.firstName("Mike")
      user.lastName("Rivera")
      user.email("foo.bar@gmail.com")
      user.save must beTrue
      user.id.get must not equalTo  -1L
    }
    "find a user by name" in {
      val user = Users.find(By(Users.firstName, "Mike"))
      user must not beEmpty
      val name = user.map(_.firstName) openOr ""
      name must beEqualTo("Mike")
    }
    "find a user by email" in {
      val user = Users.find(By(Users.email, "foo.bar@gmail.com"))

      user must not beEmpty
      val email = user.map(_.email) openOr ""
      email must beEqualTo("foo.bar@gmail.com")
    }
    "delete test user" in {
      val user = Users.find(By(Users.firstName, "Mike"))
      user must not beEmpty

      user.map(_.delete_!).openOr(false) must beTrue
    }
  }

  "the Survey mapper" should {
    args(sequential=true)
    "create test survey" in {
      val survey = Survey.create
      survey.surveyName("survey name")
      survey.save must beTrue
      survey.id.get must not equalTo  -1L
    }
    "find a survey by name" in {
      val survey = Survey.find(By(Survey.surveyName, "survey name"))
      survey must not beEmpty
      val name = survey.map(_.surveyName) openOr ""
      name must beEqualTo("survey name")
    }
    "delete survey company" in {
      val survey = Survey.find(By(Survey.surveyName, "survey name"))
      survey must not beEmpty

      survey.map(_.delete_!).openOr(false) must beTrue
    }
  }

  "the Question mapper" should {
    args(sequential=true)
    "create test question" in {
      val question = Question.create
      question.question("question 1")
      question.save must beTrue
      question.id.get must not equalTo  -1L
    }
    "find a question by question text" in {
      val question = Question.find(By(Question.question, "question 1"))
      question must not beEmpty

      val question1 = question.map(_.question) openOr ""
      question1 must beEqualTo("question 1")
    }
    "delete test question" in {
      val question = Question.find(By(Question.question, "question 1"))
      question must not beEmpty

      question.map(_.delete_!).openOr(false) must beTrue
    }
  }

  "the Answer mapper" should {
    args(sequential=true)
    "create test answer" in {
      val answer = Answer.create
      answer.answer("answer 1")
      answer.save must beTrue
      answer.id.get must not equalTo  -1L
    }
    "find a answer by answer text" in {
      val answer = Answer.find(By(Answer.answer, "answer 1"))
      answer must not beEmpty
      val answer1 = answer.map(_.answer) openOr ""
      answer1 must beEqualTo("answer 1")
    }
    "delete test answer" in {
      val answer = Answer.find(By(Answer.answer, "answer 1"))
      answer must not beEmpty

      answer.map(_.delete_!).openOr(false) must beTrue
    }
  }
}
