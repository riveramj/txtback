package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.Company
import com.riveramj.util.RandomIdGenerator.generateId
import net.liftweb.mapper.By
import net.liftweb.util.Helpers._
import org.apache.shiro.crypto.SecureRandomNumberGenerator

object CompanyService extends Loggable {
  private val rng = new SecureRandomNumberGenerator();

  def createCompany(name:String) = {
    val company = Company.create
      .companyName(name)
      .companyId(generateId)

    tryo(saveCompany(company)) flatMap {
      u => u match {
        case Full(newCompany:Company) => Full(newCompany)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def saveCompany(company:Company):Box[Company] = {

    val uniqueConstraintPattern = """.*Unique(.+)""".r
    val validateErrors = company.validate

    if (validateErrors.isEmpty) {
      tryo(company.saveMe()) match {
        case Full(newCompany:Company) => Full(newCompany)
        case Failure(_, Full(err), _) => {
          val error = err.getMessage.substring(0, err.getMessage.indexOf("\n"))
          error match {
            case uniqueConstraintPattern(x) => Failure("Company Already Exists")
            case _ => Failure("Unknown error")
          }
        }
        case _ => Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getCompanyById(companyId: String): Box[Company] = {
    Company.find(By(Company.companyId, companyId))
  }

  def deleteCompanyById(companyId: String): Box[Boolean] = {
    val company = Company.find(By(Company.companyId, companyId))
    company.map(_.delete_!)
  }

  def getCompanyByName(companyName: String): Box[Company] = {
    Company.find(By(Company.companyName, companyName))
  }

  def getAllCompanies = {
    Company.findAll()
  }

}
