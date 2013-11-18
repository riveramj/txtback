package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import com.riveramj.service.SurveyorService._
import com.riveramj.service.CompanyService._
import com.riveramj.model.Surveyor
import net.liftweb.common._
import net.liftweb.http.js.JsCmds
import com.riveramj.service.ValidationService._
import com.riveramj.model.Company
import com.riveramj.util.SecurityContext


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
    var companyName = ""

    def createUser() = {
      val validateFields = List(
        checkEmail(email, "email-error"),
        checkCompany(companyName, "company-error"),
        checkEmpty(firstName, "first-name-error"),
        checkEmpty(lastName, "last-name-error"),
        checkEmpty(password, "password-error")
      ).flatten

      if(validateFields.isEmpty) {
        val company: Box[Company] = 
          if(!companyName.isEmpty)
            createCompany(companyName)
          else
            Empty

        val user = createSurveyor(
          firstName = firstName,
          lastName = lastName,
          email = email,
          password = password,
          companyId = company.map(_._id)
        )
        
        user.map( newUser => SecurityContext.logUserIn(newUser._id))
        S.redirectTo("/home")
      }
      else {
        for (error <- validateFields) {
          S.error(error.id, error.message)
        }
      }
    }

    "#first-name" #> SHtml.text(firstName, firstName = _) &
    "#last-name" #> SHtml.text(lastName, lastName = _) &
    "#company" #> SHtml.text(companyName, companyName = _) &
    "#email" #> SHtml.text(email, email = _) &
    "#password" #> SHtml.password(password, password = _) &
    "#signup" #> SHtml.onSubmitUnit(createUser)
  }
}
