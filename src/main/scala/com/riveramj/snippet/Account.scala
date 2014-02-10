package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._
import com.riveramj.service.SurveyorService._
import com.riveramj.model.Surveyor
import net.liftweb.common._
import net.liftweb.http.js.JsCmds
import com.riveramj.service.ValidationService._
import com.riveramj.util.SecurityContext

object Account {
  import com.riveramj.util.PathHelpers.loggedIn

  val menu = Menu.i("account") / "account" >>
  loggedIn
}

class Account extends Loggable {
  val user = SecurityContext.currentUser openOrThrowException "Need Valid User"

  def edit = {
    println(user)
    var email = user.email
    var firstName = user.firstName
    var lastName = user.lastName

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
        SecurityContext.setCurrentUser(user)
      }
      else {
        for (error <- validateFields) {
          S.error(error.id, error.message)
        }
      }
    }

    "#first-name" #> SHtml.text(firstName, firstName = _) &
    "#last-name" #> SHtml.text(lastName, lastName = _) &
    "#email" #> SHtml.text(email, email = _) &
    ".save-user button" #> SHtml.onSubmitUnit(updateUser)
  }
}
