package com.riveramj.util

import net.liftweb.sitemap.Loc.If
import net.liftweb.http.RedirectResponse


object PathHelpers {

  val loggedIn = If(
    () => SecurityContext.loggedIn_?,
    () => RedirectResponse("/login")
  )

}
