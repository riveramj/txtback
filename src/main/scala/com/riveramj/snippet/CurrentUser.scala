package com.riveramj.snippet

import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import com.riveramj.util.SecurityContext

class CurrentUser {
  def name = {
    SecurityContext.currentUser map { user =>
      "*" #> (user.firstName + " " + user.lastName)
    } openOr {
      ClearNodes
    }
  }
}  
