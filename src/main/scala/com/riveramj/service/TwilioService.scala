package com.riveramj.service

import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.TwilioRestException
import com.twilio.sdk.resource.factory.SmsFactory
import com.twilio.sdk.resource.instance.Sms
import com.twilio.sdk.resource.list.SmsList
import com.twilio.sdk.resource.factory.IncomingPhoneNumberFactory
import com.twilio.sdk.resource.instance.Account
import com.twilio.sdk.resource.instance.AvailablePhoneNumber
import com.twilio.sdk.resource.instance.IncomingPhoneNumber


import net.liftweb.common.Loggable
import scala.collection.convert.WrapAsJava
import scala.collection.JavaConverters._

import net.liftweb._
import common._
import util.Props

object TwilioService extends Loggable with WrapAsJava {
  val ACCOUNT_SID = "AC7b257aff635e86c50a87e5755bf0fd79" //TODO: put in props file
  val AUTH_TOKEN = "82dca0784a423ff566696aa495f702b0" //TODO: put in props file
  val twilioClient = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN)
  val messageFactory = twilioClient.getAccount.getSmsFactory
   
  val TEST_ACCOUNT_SID = "AC5864c960b5fd10f1af07317c81dbe17a" //TODO: put in props file
  val TEST_AUTH_TOKEN = "6695764111318a945ee8a89182ac9b7a" //TODO: put in props file
  val TestTwilioClient = new TwilioRestClient(TEST_ACCOUNT_SID, TEST_AUTH_TOKEN)

  def sendMessage(toPhoneNumber: String, message: String, fromPhoneNumber: String = "7702123225") {
    val params = Map(
    "Body" -> message,
    "To" -> toPhoneNumber,
    "From" -> fromPhoneNumber
    )

    if(Props.mode == Props.RunModes.Test || Props.mode == Props.RunModes.Production) {
      messageFactory.create(mapAsJavaMap(params))  
    }
    else {
      logger.info(params)
    }
  }

  def lookupPhoneNumbers(areaCode: String, partialPhoneNumber: String = "") = {
    // Get the account and phone number factory class
    val account = twilioClient.getAccount()
    val phoneNumberFactory = account.getIncomingPhoneNumberFactory()

    // Find a number with the given area code!
    // See: http://www.twilio.com/docs/api/rest/available-phone-numbers
    val params = Map(
      "AreaCode" -> areaCode,
      "Contains" -> partialPhoneNumber
    )

    val availablePhoneNumbers = account.getAvailablePhoneNumbers(mapAsJavaMap(params)).getPageData()

    availablePhoneNumbers.asScala.map(_.getPhoneNumber())
  }

  def buyPhoneNumber(phoneNumber: String) = {
    // https://www.twilio.com/docs/howto/search-and-buy
    // test number to buy is +15005550006
    val purchaseParams = Map(
      "PhoneNumber" -> s"+1$phoneNumber"
    )

    val purchasedNumber = TestTwilioClient.getAccount.getIncomingPhoneNumberFactory().create(mapAsJavaMap(purchaseParams))
    
    purchasedNumber.getPhoneNumber()
  }
}
