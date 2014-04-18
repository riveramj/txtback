package com.riveramj.snippet

import com.riveramj.service.SurveyorService._
import com.riveramj.service.{TwilioService, PhoneNumberService}
import com.riveramj.model.Surveyor
import com.riveramj.service.ValidationService._
import com.riveramj.util.SecurityContext

import net.liftweb.common._
import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.util.ClearClearable
import net.liftweb.http.js.JsCmds
import net.liftweb.http.IdMemoizeTransform

import scala.xml.NodeSeq


object Account {
  import com.riveramj.util.PathHelpers.loggedIn

  val menu = Menu.i("account") / "account" >>
  loggedIn
}

class Account extends Loggable {
  val user = SecurityContext.currentUser openOrThrowException "Need Valid User"

  def edit = {
    var email = user.email
    var firstName = user.firstName
    var lastName = user.lastName

    var availableNumbers: List[String] = Nil
    var phoneNumberRadios: NodeSeq = Nil 
    var selectedNumber = ""
    var areaCode = ""
    var phone = ""

    def updateUser() = {
      val emailError = 
        if(email != user.email)
          checkEmail(email, "email-error")
        else 
          Empty


      val validateFields = List(
        checkEmpty(firstName, "first-name-error"),
        checkEmpty(lastName, "last-name-error"),
        emailError
      ).flatten

      if(validateFields.isEmpty) {
        val updatedUser = user.copy(
          firstName = firstName,
          lastName = lastName,
          email = email
        )

        saveUser(updatedUser)
      }
      else {
        for (error <- validateFields) {
          S.error(error.id, error.message)
        }
      }
    }

    def findPhoneNumbers(renderer: IdMemoizeTransform) = {
      println(areaCode)
      println(phone)
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
    "#email" #> SHtml.text(email, email = _) &
    "#available-numbers" #> user.phoneNumbers.map { number => 
      ".available-number *" #> number
    } &
    ".available-numbers"  #> SHtml.idMemoize { renderer =>
      "#area-code" #> SHtml.ajaxText(areaCode, areaCode = _) &
      "#phone" #> SHtml.ajaxText(phone, phone = _) &
      ".number-search [onclick]" #> SHtml.ajaxInvoke(() => findPhoneNumbers(renderer)) &
      ".number-entry" #> phoneNumberRadios.map { radio => 
        "input" #> radio
      }
    } &
    ".save-user button" #> SHtml.onSubmitUnit(updateUser)
  }
}
