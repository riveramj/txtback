package com.riveramj.service

import net.liftweb.common._

object PhoneNumberService extends Loggable {

  def formatPhoneNumber(number: String) = {
    val realNumber =
      if (number.startsWith("+1"))
        number.substring(2)
      else
        number

    realNumber.filter(_.toString.matches("\\d"))
  }
}
