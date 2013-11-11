package com.riveramj.snippet

import net.liftweb.sitemap.Menu
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._
import com.riveramj.service.SurveyorService._
import com.riveramj.model.Surveyor
import net.liftweb.common._
import com.riveramj.util.SecurityContext
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmds
import net.liftweb.util.Props


object Login {
  val menu = Menu.i("login") / "login"
}
class Login extends Loggable {


  def userCanLogIn_?(user:Surveyor, password:String): Boolean = {
    if(user.password == hashPassword(password, user.salt)) 
      true
    else 
      false    
  }

  def render = {
    var email = ""
    var password = ""

    def login() = {
      getUserByEmail(email) match {
        case Full(user) =>
          if(userCanLogIn_?(user, password)) {
            SecurityContext.logUserIn(user._id)
            S.redirectTo("/home")
          }
          else
           S.redirectTo("/login") 
        case failure => logger.error("failure with info %s".format(failure)) //TODO: return message back if failed to login

      }
    }

    "#username" #> SHtml.text(email, email= _) &
    "#password" #> SHtml.password(password, password = _) &
    "type=submit" #> SHtml.onSubmitUnit(login)
  }

}
