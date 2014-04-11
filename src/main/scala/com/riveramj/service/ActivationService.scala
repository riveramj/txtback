package com.riveramj.service


import com.riveramj.model.Surveyor

import java.security.SecureRandom

import scala.util.Random

import net.liftweb.util.StringHelpers
import net.liftweb.common._
import net.liftweb.json.JsonDSL._

object ActivationService extends Loggable {
  private val randomGenerator = new Random(new SecureRandom)

  def createActivationKey() = {
    StringHelpers.randomString(16)
  }

  def getUserByActivationKey(key: String) = {
    Surveyor.find("activationKey" -> key) 
  }

  def activateSurveyor(user: Surveyor) = {
    SurveyorService.saveUser(user.copy(
      active = true,
      activationKey = None, 
      activationKeyDate = None
    ))
  }

  def removeActivationKey(user: Surveyor) = {
    SurveyorService.saveUser(user.copy(
      activationKey = None, 
      activationKeyDate = None      
    ))
  }
}
