package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import com.riveramj.service.SurveyorService._
import com.riveramj.service.TwilioService
import com.riveramj.model.Surveyor
import net.liftweb.common._
import net.liftweb.util.ClearClearable
import net.liftweb.http.js.JsCmds
import net.liftweb.http.IdMemoizeTransform
import com.riveramj.service.ValidationService._
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
    var areaCode = ""
    var phone = ""
    var availableNumbers: List[String] = Nil 
    

    def signupUser() = {
      val validateFields = List(
        checkEmail(email, "email-error"),
        checkEmpty(firstName, "first-name-error"),
        checkEmpty(lastName, "last-name-error"),
        checkEmpty(password, "password-error"),
        checkEmpty(areaCode, "area-code-error"),
        checkEmpty(phone, "phone-error"),
        checkValidNumber(areaCode+phone, "phone-number-error")
      ).flatten

      if(validateFields.isEmpty) {
        val purchasedPhoneNumber = TwilioService.buyPhoneNumber(areaCode+phone)
        
        val user = createSurveyor(
          firstName = firstName,
          lastName = lastName,
          email = email,
          password = password, 
          phoneNumber = purchasedPhoneNumber
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

    def findPhoneNumbers(renderer: IdMemoizeTransform) = {
      availableNumbers = TwilioService.lookupPhoneNumbers(areaCode, phone).toList
      renderer.setHtml
    }

    ClearClearable andThen
    "#first-name" #> SHtml.text(firstName, firstName = _) &
    "#last-name" #> SHtml.text(lastName, lastName = _) &
    "#email" #> SHtml.text(email, email = _) &
    "#password" #> SHtml.password(password, password = _) &
    ".available-numbers"  #> SHtml.idMemoize { renderer =>
      "#area-code" #> SHtml.ajaxText(areaCode, areaCode = _) &
      "#phone" #> SHtml.ajaxText(phone, phone = _) &
      ".number-search [onclick]" #> SHtml.ajaxInvoke(() => findPhoneNumbers(renderer)) &
      ".number-entry" #> availableNumbers.map { availableNumber =>
        "div [id]" #> availableNumber &
        "div *" #> availableNumber &
        "input [value]" #> availableNumber
      }
    } &
    ".signup button" #> SHtml.onSubmitUnit(signupUser)
  }
}
