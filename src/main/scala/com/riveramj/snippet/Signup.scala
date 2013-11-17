package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import com.riveramj.service.SurveyorService._
import com.riveramj.model.Surveyor
import net.liftweb.common._
import net.liftweb.http.js.JsCmds
import com.riveramj.service.ValidationService._


object Signup {
  val menu = Menu.i("signup") / "signup"
}
class Signup extends Loggable with StatefulSnippet {

  def dispatch = {case _ => render}

  def render = {
    var email = ""
    var password = ""
    var firstName = ""
    var lastName = ""
    var company = ""

    def createUser() = {
      val validateFields = List(
        checkEmail(email, "email-error"),
        checkCompany(company, "company-error"),
        checkEmpty(firstName, "first-name-error"),
        checkEmpty(lastName, "last-name-error"),
        checkEmpty(password, "password-error")
      ).flatten

      for (error <- validateFields) {
        S.error(error.id, error.message)
      }
    }

    "#first-name" #> SHtml.text(firstName, firstName= _) &
    "#last-name" #> SHtml.text(lastName, lastName= _) &
    "#company" #> SHtml.text(company, company= _) &
    "#email" #> SHtml.text(email, email= _) &
    "#password" #> SHtml.password(password, password = _) &
    "#signup" #> SHtml.onSubmitUnit(createUser)
  }

}
