package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._
import com.riveramj.service.UserService._
import com.riveramj.model.Users
import net.liftweb.common.{Failure, Box, Full}


object Login {
  val menu = Menu.i("login") / "login"
}
class Login {


  def userCanLogIn_?(user:Users, password:String): Box[Boolean] = {
    if (user.password.get != hashPassword(password, user.salt.get)) {
      println("failed to match ")
      Full(false)
    } else {
      Full(true)
    }
  }

  def render = {
    var email = ""
    var password = ""

    def login() = {
      getUserByEmail(email) match {
        case Full(user) => println("legit"); S.redirectTo("/home")
        case failure => println("failure with info %s".format(failure))

      }
    }

    "#username" #> SHtml.text(email, email= _) &
    "#password" #> SHtml.password(password, password = _) &
    "type=submit" #> SHtml.onSubmitUnit(login)
  }

}
