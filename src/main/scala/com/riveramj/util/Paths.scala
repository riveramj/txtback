package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common._
import net.liftweb.http.RedirectResponse
import com.riveramj.snippet._
import com.riveramj.util.PathHelpers.loggedIn


object Paths {
  // Roots.
  val index          = Menu.i("index")   / "index" >>
    EarlyResponse(() => Full(RedirectResponse(Home.menu.loc.calcDefaultHref)))

  def siteMap = SiteMap(
    index,
    Login.menu,
    Home.menu >> loggedIn,
    SurveySnippet.menu,
    SurveyResponsesSnippet.menu
  )
}
