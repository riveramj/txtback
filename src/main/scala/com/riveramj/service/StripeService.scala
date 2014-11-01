package com.riveramj.service

import net.liftweb.common._
import net.liftweb.util.Props

import com.stripe

object StripeService extends Loggable {

  stripe.apiKey = Props.get("stripe.test.key").openOr("")
  stripe.setPublishableKey = Props.get("stripe.test.key").openOr("")

  def createCustomer(email: String, cardNumber: String, cvc: String, expirationDate: String, zipCode: String) = {
    val userCardCollection = CardCollection(1, Map(
      "number" -> cardNumber,
      "cvc" -> cvc,
      "exp_month" -> expirationDate.take(2),
      "exp_year" -> expirationDate.drop(3),
      "address_zip" -> zipCode
    )

    println(userCard)
    
    val newCustomer = stripe.Customer.create(Map(
      "email" -> email,
      "account_balance" -> -5,
      "card" -> userCard 
    ))

    println(newCustomer)
    newCustomer
  }
  
}
