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


object Signup {
  val menu = Menu.i("signup") / "signup"
}
class Signup extends Loggable {
  
}
