package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._
import com.riveramj.service.UserService._
import com.riveramj.model.Users
import net.liftweb.common._
import com.riveramj.util.SecurityContext
import net.liftweb.common.Full


object Login {
  val menu = Menu.i("login") / "login"
}
class Login extends Loggable {


  def userCanLogIn_?(user:Users, password:String): Box[Boolean] = {
    if (user.password.get != hashPassword(password, user.salt.get)) {
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
        case Full(user) =>
          SecurityContext.logUserIn(user.userId.get)
          S.redirectTo("/home")
        case failure => logger.error("failure with info %s".format(failure))

      }
    }

    "#username" #> SHtml.text(email, email= _) &
    "#password" #> SHtml.password(password, password = _) &
    "type=submit" #> SHtml.onSubmitUnit(login)
  }

}
