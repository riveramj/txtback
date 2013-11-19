package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.Company
import net.liftweb.util.Helpers._
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._
import java.util.regex._

object CompanyService extends Loggable {

  def createCompany(name: String, shadow: Boolean = false) = {
    val company = Company(
      ObjectId.get,
      name,
      shadow
    )

    saveCompany(company)
  }

  def saveCompany(company:Company): Box[Company] = {
    company.save
    getCompanyById(company._id)
  }

  def getCompanyById(companyId: ObjectId): Box[Company] = {
    Company.find(companyId)
  }

  def deleteCompanyById(companyId: ObjectId) = {
    getCompanyById(companyId).map(_.delete)
  }

  def getCompanyByName(companyName: String): Box[Company] = {
    val pattern = Pattern.compile(companyName, Pattern.CASE_INSENSITIVE)
    Company.find("name" -> (("$regex" -> pattern.pattern) ~ ("$flags" -> pattern.flags)))
  }

  def getAllCompanies = {
    Company.findAll
  }

}
