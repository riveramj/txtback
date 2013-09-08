package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.Company
import net.liftweb.util.Helpers._
import org.bson.types.ObjectId
import net.liftweb.json.JsonDSL._

object CompanyService extends Loggable {

  def createCompany(name:String) = {
    val company = Company(
      ObjectId.get,
      name
    )

    saveCompany(company)
  }

  def saveCompany(company:Company): Box[Company] = {
    company.save
    Company.find(company._id)
  }

  def getCompanyById(companyId: ObjectId): Box[Company] = {
    Company.find(companyId)
  }

  def deleteCompanyById(companyId: ObjectId) = {
    val company = getCompanyById(companyId)
    company.map(_.delete)
  }

  def getCompanyByName(companyName: String): Box[Company] = {
    Company.find("name" -> companyName)
  }

  def getAllCompanies = {
    Company.findAll
  }

}
