package com.riveramj.service

import com.riveramj.service.SurveyorService._
import com.riveramj.service.CompanyService._
import net.liftweb.common._
import net.liftweb.http.S

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

  def checkDuplicateCompany(companyName: String, errorId: String): Box[ValidationError] = {
    getCompanyByName(companyName) match {
      case Full(company) => Full(ValidationError(errorId, S ? "Company Already Exists"))
      case _ => Empty
    }
  }

  def checkEmail(email: String, errorId: String): Box[ValidationError] = {
    checkDuplicateEmail(email, errorId) or
    checkEmpty(email, errorId)
  }

  def checkCompany(companyName: String, errorId: String): Box[ValidationError] = {
    checkDuplicateCompany(companyName, errorId)
  }
}

case class ValidationError(id: String, message: String)
