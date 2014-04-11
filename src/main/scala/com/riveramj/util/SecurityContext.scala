package com.riveramj.util

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.ControlHelpers._
import com.riveramj.model.Surveyor
import com.riveramj.service.SurveyorService._
import org.bson.types.ObjectId

object SecurityContext extends Loggable {
  private object loggedInUserId extends SessionVar[Box[ObjectId]](Empty)
  private object loggedInUser extends RequestVar[Box[Surveyor]](Empty)

  def logIn(user: Surveyor) {
    setCurrentUser(user)
    loggedInUser.is

    logger.info("Logged user in [ %s ]".format(user.email ))
  }

  def logCurrentUserOut {
    loggedInUserId(Empty)
    loggedInUser(Empty)
  }


  def setCurrentUser(user: Surveyor) {
    loggedInUserId(Full(user._id))
    loggedInUser(Full(user))
  }

  def clearCurrentUser() {
    loggedInUserId(Empty)
    loggedInUser(Empty)
  }

  def logUserIn(userId: ObjectId): Boolean = {
    getUserById(userId) match {
      case Full(user) =>
        logIn(user)
        true
      case _ =>
        false
    }
  }

  def currentUser: Box[Surveyor] = {
    loggedInUser.is or loggedInUserId.is.flatMap { userId =>
      val user = getUserById(userId)
      loggedInUser(user)

      loggedInUser.is
    }
  }

  def currentUserId: Box[ObjectId] = {
    currentUser.map(_._id)
  }

  def currentUserName: Box[String] = {
    for {
      user <- currentUser
      firstName = user.firstName
      lastName = user.lastName
    } yield {
      firstName + " " + lastName
    }
  }

  def loggedIn_? : Boolean = {
    currentUser.isDefined
  }
}
