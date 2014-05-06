package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import net.liftweb.util.Helpers._
import net.liftweb.util.Props
import net.liftweb.common._
import net.liftweb.util.ClearClearable
import net.liftweb.http.js.JsCmds
import net.liftweb.http.IdMemoizeTransform

import com.riveramj.service.SurveyorService._
import com.riveramj.service.{PhoneNumberService, TwilioService}
import com.riveramj.model.{Surveyor, PhoneNumber}
import com.riveramj.service.ValidationService._
import com.riveramj.util.SecurityContext

import scala.xml.NodeSeq

object Signup {
  val menu = Menu.i("signup") / "signup"
}
class Signup extends Loggable with StatefulSnippet {
  val testPhoneNumber = Props.get("test.phone.number").openOr("")
  val formattedTestNumber = PhoneNumberService.longFormatPhoneNumber(testPhoneNumber)

  def dispatch = {case _ => render}

  def render = {
    var email = ""
    var password = ""
    var firstName = ""
    var lastName = ""
    var areaCode = ""
    var phone = ""
    var availableNumbers: List[String] = Nil
    var phoneNumberRadios: NodeSeq = Nil 
    var selectedNumber = ""
    

    def signupUser() = {
      val validateFields = List(
        checkEmail(email, "email-error"),
        checkEmpty(firstName, "first-name-error"),
        checkEmpty(lastName, "last-name-error"),
        checkEmpty(password, "password-error"),
        checkEmpty(selectedNumber, "phone-number-error"),
        checkValidNumber(selectedNumber, "phone-number-error")
      ).flatten

      if(validateFields.isEmpty) {
        val purchasedPhoneNumber = (selectedNumber == formattedTestNumber) match {
          case true => PhoneNumber(number = selectedNumber, sid = "")
          case false => TwilioService.buyPhoneNumber(selectedNumber)
        }

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
      val rawNumbers = TwilioService.lookupPhoneNumbers(areaCode, phone)
      availableNumbers = rawNumbers.map { number => 
        PhoneNumberService.longFormatPhoneNumber(number)
      }

      phoneNumberRadios = SHtml.radio(availableNumbers.toSeq, Empty, selectedNumber = _).toForm 

      renderer.setHtml
    }

    ClearClearable andThen
    "#first-name" #> SHtml.text(firstName, firstName = _) &
    "#last-name" #> SHtml.text(lastName, lastName = _) &
    "#email" #> SHtml.text(email, userEmail => email = userEmail.trim) &
    "#password" #> SHtml.password(password, password = _) &
    ".available-numbers"  #> SHtml.idMemoize { renderer =>
      "#area-code" #> SHtml.ajaxText(areaCode, areaCode = _) &
      "#phone" #> SHtml.ajaxText(phone, phone = _) &
      ".number-search [onclick]" #> SHtml.ajaxInvoke(() => findPhoneNumbers(renderer)) &
      ".number-entry" #> phoneNumberRadios.map { radio => 
        "input" #> radio
      }
    } &
    ".signup button" #> SHtml.onSubmitUnit(signupUser)
  }
}
