package com.riveramj.service

import net.liftweb.util.Mailer
import Mailer._
import net.liftweb.common._

object MailService extends Loggable { 

  def sendMail(from: String, to: String, subject: String, body: String) {
    Mailer.sendMail(
      From(from),
      Subject(subject),
      To(to),
      PlainMailBodyType(body)
    ) 
  }
}

