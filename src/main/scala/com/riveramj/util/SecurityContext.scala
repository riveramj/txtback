package com.riveramj.util

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.ControlHelpers._
import com.riveramj.model.{Company, Surveyor}
import com.riveramj.service.SurveyorService._
import com.riveramj.service.CompanyService._

/**
 * Provides the security context information for the current session.
 *
 * All determinations of who the current user is should go through this singleton. When a user
 * logs in, their user ID is stored in the current session, but the actual user object is refreshed
 * with each full-page load. AJAX requests should be in the same request scope and use the same
 * instance of the user without retrieving a new one from Scribe.
 */
object SecurityContext extends Loggable {
  private object loggedInUserId extends SessionVar[Box[Long]](Empty)
  private object loggedInCompanyId extends SessionVar[Box[Long]](Empty)

  private object loggedInUser extends RequestVar[Box[Surveyor]](Empty)
  private object loggedInUserCompany extends RequestVar[Box[Company]](Empty)



  def logIn(user: Surveyor) {

    setCurrentUser(user)

    // We immediately access the request var to suppress the warning about it
    // never being accessed during a request.
    loggedInUser.is

    logger.info("Logged user in [ %s ]".format(user.email.get ))
  }


  def setCurrentUser(user: Surveyor) {
    loggedInUserId(Full(user.userId.get))
    loggedInUser(Full(user))
  }

  def clearCurrentUser() {
    loggedInUserId(Empty)
    loggedInUser(Empty)
  }

  def logUserIn(userId: Long): Boolean = {
    getUserById(userId) match {
      case Full(user) =>
        logIn(user)
        true
      case _ =>
        false
    }
  }

  def logCurrentUserOut() : Boolean = {
    {
      for {
        user <- currentUser
        email = user.email.get
      } yield {
        logger.info("Logged user out [" + email + "]")
        clearCurrentUser()
        true
      }
    } openOr false
  }

  def currentUser: Box[Surveyor] = {
    loggedInUser.is or loggedInUserId.is.flatMap { userId =>
      val user = getUserById(userId)
      loggedInUser(user)

      // We immediately access the request var to suppress the warning about it
      // never being accessed during a request.
      loggedInUser.is
    }
  }

  def currentUserName: Box[String] = {
    for {
      user <- currentUser
      firstName = user.firstName.get
      lastName = user.lastName.get
    } yield {
      firstName + " " + lastName
    }
  }

  def currentCompany : Box[Company] = {
    loggedInUserCompany.is or {
      val currentCompanyId = currentUser.map(_.companyId.get)
      val currentCompany = getCompanyById(currentCompanyId openOr 0L)

      loggedInCompanyId(currentCompanyId)
      loggedInUserCompany(currentCompany)

      loggedInUserCompany.is
    }
  }

  def currentCompanyId : Box[Long] = loggedInUserCompany map (_.companyId.get)

  def loggedIn_? : Boolean = {
    currentUser.isDefined
  }
}