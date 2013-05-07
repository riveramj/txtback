package com.riveramj.util

import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.common._
import net.liftweb.http.RedirectResponse
import com.riveramj.snippet.Login

object Paths {
  val static = Menu.i("Static") / "static" / **

  // Roots.
  val index          = Menu.i("home")   / "index" >>
    EarlyResponse(() => Full(RedirectResponse(home.loc.calcDefaultHref)))
  val home  = Menu.i("menu-home") / "home"

  def siteMap = SiteMap(
    static,
    index,
    Login.menu
  )
}
