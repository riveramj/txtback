package com.riveramj.util

import net.liftweb.common.Loggable
import bootstrap.liftweb.Boot
import com.riveramj.service.CompanyService._
import com.riveramj.service.UserService

object TestDataLoader extends Loggable {

  def initialize = {
    val boot = new Boot
    boot.boot
  }



  def main(args: Array[String]) {
    initialize
    val testData = new TestDataLoader
    testData.populateTestUsers()
  }
}

class  TestDataLoader extends  Loggable {
  def populateTestUsers() {
    val company = createCompany("Company1")

    val companyId = company.map(comp => comp.companyId.get) getOrElse 0L
    populateTestUser("Mike", "Rivera", "rivera.mj@gmail.com", companyId , "password")
  }

  private def populateTestUser(firstName: String, lastName: String, email: String, companyId: Long, password: String) {
    UserService.createUser(firstName, lastName, email, companyId, password)
  }
}
