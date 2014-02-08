package com.riveramj.service

import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.TwilioRestException
import com.twilio.sdk.resource.factory.SmsFactory
import com.twilio.sdk.resource.instance.Sms
import com.twilio.sdk.resource.list.SmsList
import net.liftweb.common.Loggable
import scala.collection.convert.WrapAsJava

import net.liftweb._
import common._
import util.Props

object TwilioService extends Loggable with WrapAsJava {
  val ACCOUNT_SID = "AC7b257aff635e86c50a87e5755bf0fd79" //TODO: put in props file
  val AUTH_TOKEN = "82dca0784a423ff566696aa495f702b0" //TODO: put in props file
  val twilioClient = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN)
  val messageFactory = twilioClient.getAccount.getSmsFactory

  def sendMessage(toPhoneNumber: String, message: String, fromPhoneNumber: String = "7702123225") {
    val params = Map(
    "Body" -> message,
    "To" -> toPhoneNumber,
    "From" -> fromPhoneNumber
    )

    if(Props.mode == Props.RunModes.Test || Props.mode == Props.RunModes.Production)
      messageFactory.create(mapAsJavaMap(params))  
    else
      logger.info(params)
  }
}
