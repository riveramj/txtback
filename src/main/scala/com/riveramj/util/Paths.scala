package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common._
import net.liftweb.http.{RedirectResponse,S}

import com.riveramj.snippet._
import com.riveramj.util.PathHelpers.loggedIn


object Paths {
  // Roots.
  val index = Menu.i("index")   / "index" >>
    EarlyResponse(() => Full(RedirectResponse(Surveys.menu.loc.calcDefaultHref)))

  val home = Menu.i("home")   / "home" >>
    EarlyResponse(() => Full(RedirectResponse(Surveys.menu.loc.calcDefaultHref)))

  val logoutMenu = Menu.i("logout") / "logout" >>
    EarlyResponse(logout _)

  def logout() = {
    SecurityContext.logCurrentUserOut
    S.session.foreach(_.destroySession())
    Full(RedirectResponse(Login.menu.loc.calcDefaultHref))
  }


  def siteMap = SiteMap(
    home,
    index,
    logoutMenu,
    Login.menu,
    Account.menu,
    Signup.menu,
    Surveys.menu >> loggedIn,
    SurveySnippet.menu,
    SurveyResponsesSnippet.menu,
    ActivateUser.menu
  )
}
