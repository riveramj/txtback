package com.riveramj.snippet

import net.liftweb.sitemap._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http._
import net.liftweb.util.Helpers._

import com.riveramj.service.ActivationService
import com.riveramj.model.Surveyor

object ActivateUser {
  import com.riveramj.util.PathHelpers.loggedIn

  lazy val menu = Menu.param[String]("Activate User","Activate User",
    Full(_),
    (id) => id
  ) / "activate" / * >>
  TemplateBox(() => Templates( "activate-user" :: Nil))
}

class ActivateUser extends Loggable {
  import ActivateUser._

  val activationKey = menu.currentValue openOrThrowException "Not valid activation key"
  
  def render() = {
    {
      for {
        user <- ActivationService.getUserByActivationKey(activationKey)
        activatedUser <- ActivationService.activateSurveyor(user)
      } yield {
        activatedUser
      }
    } match {
      case Some(user) =>
        S.redirectTo(Surveys.menu.loc.calcDefaultHref)
      case _ => 
        S.redirectTo(Login.menu.loc.calcDefaultHref)
    }

    "*" #> ""  

  }
  
}
