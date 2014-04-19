package com.riveramj.service

import com.twilio.sdk.{TwilioRestClient, TwilioRestException}
import com.twilio.sdk.resource.instance.Sms
import com.twilio.sdk.resource.list.SmsList
import com.twilio.sdk.resource.factory.{IncomingPhoneNumberFactory, SmsFactory}
import com.twilio.sdk.resource.instance.{Account, AvailablePhoneNumber, IncomingPhoneNumber}

import net.liftweb.common._
import net.liftweb.util.Props

import scala.collection.convert.WrapAsJava
import scala.collection.JavaConverters._

import com.riveramj.model.PhoneNumber
import com.riveramj.util.SecurityContext


object TwilioService extends Loggable with WrapAsJava {
  val accountSid = Props.get("twilio.account.sid").openOr("")
  val authToken =  Props.get("twilio.auth.token").openOr("")
  
  val twilioClient = new TwilioRestClient(accountSid, authToken)
  val account = twilioClient.getAccount()
  val smsFactory = account.getSmsFactory()
  val accountFactory = twilioClient.getAccountFactory()
  
  val testAccountSid = Props.get("twilio.test.account.sid").openOr("")
  val testAuthToken =  Props.get("twilio.test.auth.token").openOr("")

  val testTwilioClient = new TwilioRestClient(testAccountSid, testAuthToken)
  val testAccount = testTwilioClient.getAccount()
  
  val testPhoneNumber = Props.get("test.phone.number").openOr("")

  val incomingPhoneNumberFactory = 
    (Props.mode == Props.RunModes.Development || Props.mode == Props.RunModes.Pilot) match {
      case true => testAccount.getIncomingPhoneNumberFactory()
      case false => account.getIncomingPhoneNumberFactory()
    }

  def sendMessage(toPhoneNumber: String, message: String, fromPhoneNumber: String = "7702123225") {
    val params = Map(
    "Body" -> message,
    "To" -> toPhoneNumber,
    "From" -> fromPhoneNumber
    )

    (Props.mode == Props.RunModes.Development) match {
      case true => logger.info(params)
      case false => smsFactory.create(mapAsJavaMap(params))  
    }
  }

  def lookupPhoneNumbers(areaCode: String, partialPhoneNumber: String = "") = {
    // Get the account and phone number factory class
    
    val phoneNumberFactory = account.getIncomingPhoneNumberFactory()

    // Find a number with the given area code!
    // See: http://www.twilio.com/docs/api/rest/available-phone-numbers
    val params = Map(
      "AreaCode" -> areaCode,
      "Contains" -> partialPhoneNumber
    )

    val availablePhoneNumbers = account.getAvailablePhoneNumbers(mapAsJavaMap(params)).getPageData()

    List(testPhoneNumber) ++ availablePhoneNumbers.asScala.map(_.getPhoneNumber()).toList
  }

  def buyPhoneNumber(phoneNumber: String) = {
    // https://www.twilio.com/docs/howto/search-and-buy
    // test number to buy is +15005550006
    val purchaseParams = Map(
      "PhoneNumber" -> s"+1$phoneNumber"
    )

    val purchasedNumber = incomingPhoneNumberFactory.create(mapAsJavaMap(purchaseParams))
    
    purchasedNumber.getPhoneNumber()
    PhoneNumber(sid = purchasedNumber.getSid(), number = purchasedNumber.getPhoneNumber()) 
  }
  }

  def createSubAccount(email: String) = {
    // Build a filter for the AccountList
    val subAccountParams = Map("FriendlyName" -> email)
     
    val account = accountFactory.create(mapAsJavaMap(subAccountParams))
    account.getSid()
  }
}
