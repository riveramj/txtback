package code.model

import org.specs2.mutable.Specification
import bootstrap.liftweb.Boot
import net.liftweb.mapper.By
import com.riveramj.model._

class ModelSpecs extends Specification {

  step {
    val boot = new Boot
    boot.boot

    //don't use proxy
    java.lang.System.clearProperty("http.proxyHost")
    java.lang.System.clearProperty("https.proxyHost")
  }

  "the Company mapper" should {
    "generate a unique company ID" in {
      Company.create.companyId must not beNull
    }
  }

  "the Survey mapper" should {
    "generate a unique survey ID" in {
      Survey.create.surveyId must not beNull
    }
  }

  "the Question mapper" should {
    "generate a unique question ID" in {
      Question.create.questionId must not beNull
    }
  }

  "the Answer mapper" should {
    "generate a unique Answer ID" in {
      Answer.create.answerId must not beNull
    }
  }
}
