package code.service

import org.specs2.mutable.Specification
import bootstrap.liftweb.Boot
import com.riveramj.service.CompanyService._
import net.liftweb.common.{Full}


class CompanyServiceSpecs extends Specification {
  // 
  // step {
  //   val boot = new Boot
  //   boot.boot
  // }
  // 
  // "the Company Service" should {
  //   args(sequential=true)
  // 
  //   "create company" in {
  //     val company = createCompany("Mike Company")
  //     company must not beEmpty
  //   }
  //   "find company by name" in {
  //     val company = getCompanyByName("Mike Company")
  //     company must not beEmpty
  // 
  //     company.map(_.name).openOr("") must beEqualTo("Mike Company")
  //   }
  //   "find company by id" in {
  //     val company = getCompanyByName("Mike Company")
  //     company must not beEmpty
  // 
  //     val companyName = company.map(_.name)
  //     companyName must beEqualTo(Full("Mike Company"))
  // 
  //     val companyId = company.map(_._id)
  // 
  //     val companyById = getCompanyById(companyId.open_!)
  //     companyById.map(_.name).getOrElse("") must beEqualTo("Mike Company")
  //   }
  // 
  //   "find all companies" in {
  //     val companies = getAllCompanies
  //     companies must not beEmpty
  //   }
  // 
  //   "delete company by id" in {
  // 
  //     val company = getCompanyByName("Mike Company")
  //     company must not beEmpty
  // 
  //     val companyName = company.map(_.name)
  //     companyName must beEqualTo(Full("Mike Company"))
  // 
  //     val companyId = company.map(_._id).open_!
  // 
  //     deleteCompanyById(companyId).openOr(false) must beTrue
  //   }
  // }
}
