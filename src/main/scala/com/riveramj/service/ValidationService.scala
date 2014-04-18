package com.riveramj.service

import com.riveramj.service.SurveyorService._
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.util.Props

object ValidationService extends Loggable {

  def checkEmpty(fieldValue: Option[String], fieldId: String): Box[ValidationError] = {
    checkEmpty(fieldValue getOrElse "", fieldId)
  }

  def checkEmpty(fieldValue: String, fieldId: String): Box[ValidationError] = {
    if (fieldValue.trim.nonEmpty) {
      Empty
    } else {
      Full(ValidationError(fieldId, S ? "Field Required"))
    }
  }

  def checkDuplicateEmail(email: String, errorId: String): Box[ValidationError] = {
    if (email.nonEmpty) {
      SurveyorService.getUserByEmail(email) match {
        case Full(user)  => Full(ValidationError(errorId, S ? "Email already exists"))
        case _ => Empty
      }
    } else Empty
  }

  def checkEmail(email: String, errorId: String): Box[ValidationError] = {
    checkDuplicateEmail(email, errorId) or
    checkEmpty(email, errorId)
  }

  def checkValidNumber(phoneNumber: String, errorId: String): Box[ValidationError] = {
    val testPhoneNumber = Props.get("test.phone.number").openOr("")
    val formattedNumber = PhoneNumberService.longFormatPhoneNumber(testPhoneNumber)
    
    if (phoneNumber != formattedNumber) {
      Full(ValidationError(errorId, S ? s"Use $formattedNumber for testing."))
    } else {
      Empty 
    }
  }
}

case class ValidationError(id: String, message: String)
