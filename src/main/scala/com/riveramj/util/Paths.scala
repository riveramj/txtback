package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.{If, EarlyResponse}
import net.liftweb.common._
import net.liftweb.http.RedirectResponse
import com.riveramj.snippet.Login
import com.riveramj.snippet.SurveySnippet
import com.riveramj.snippet.Home

object Paths {
  // Roots.
  val index          = Menu.i("index")   / "index" >>
    EarlyResponse(() => Full(RedirectResponse(Home.menu.loc.calcDefaultHref)))

  def siteMap = SiteMap(
    index,
    Login.menu,
    Home.menu >> loggedIn,
    SurveySnippet.menu >> loggedIn
  )

   val loggedIn = If(
    () => SecurityContext.loggedIn_?,
     () => RedirectResponse("/login")
  )
}
