package bootstrap.liftweb

import net.liftweb.util.Props
import net.liftweb.common._
import net.liftweb.util._
import javax.mail.{Authenticator, PasswordAuthentication}

object MailConfig {

  def init = {
    (Props.get("mail.user"), Props.get("mail.password")) match {
      case (Full(username), Full(password)) =>
      Mailer.authenticator = Full(new Authenticator() {
        override def getPasswordAuthentication = new
        PasswordAuthentication(username, password)
      })
      case _ => throw new Exception("Username/password not supplied for Mailer.")
    }
  }
}
