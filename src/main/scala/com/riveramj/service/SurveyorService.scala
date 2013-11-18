package com.riveramj.service

import com.riveramj.model.Surveyor
import com.riveramj.service.MailService._
import net.liftweb.util.Props

import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import org.bson.types.ObjectId

import org.apache.shiro.crypto.hash.Sha256Hash
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import java.util.regex._


object SurveyorService extends Loggable {

  def hashPassword(password: String, salt: String): String = {
    new Sha256Hash(password, salt, 1024).toBase64
  }

  private def getSalt: String = {
    val rng = new SecureRandomNumberGenerator()
    rng.nextBytes(32).toBase64
  }

  def createSurveyor(firstName: String, lastName: String, email: String, companyId: Option[ObjectId], password: String) = {

    val salt = getSalt
    val hashedPassword = hashPassword(password, salt)

    val user = Surveyor(
      _id = ObjectId.get,
      firstName = firstName,
      lastName = lastName,
      email = email,
      companyId = companyId,
      password = hashedPassword,
      salt = salt
    )

    val newUser = saveUser(user)
    confirmUserEmail(newUser)
    newUser
  }

  def saveUser(user:Surveyor): Box[Surveyor] = {
    user.save
    Surveyor.find(user._id)
  }

  def getUserById(userId: ObjectId): Box[Surveyor] = {
    Surveyor.find(userId)
  }

  def deleteUserById(userId: ObjectId) = {
    val user = getUserById(userId)
    user.map(_.delete)
  }

  def getUserByEmail(email: String): Box[Surveyor] = {
    val pattern = Pattern.compile(email, Pattern.CASE_INSENSITIVE)
    Surveyor.find("email" -> (("$regex" -> pattern.pattern) ~ ("$flags" -> pattern.flags)))
  }

  def getAllUsers = {
    Surveyor.findAll
  }

  def confirmUserEmail(user: Box[Surveyor]) = {
    val fromEmail = Props.get("info.email") match {
      case Full(email) => email
      case _ => throw new Exception("Info email not supplied")
    }

    sendMail(
      from = fromEmail,
      to = user.map(_.email).openOr(""),
      subject = "Welcome to TxtBack! Please confirm your email",
      body = "Welcome to TxtBack! Please confirm your email"
    )
  }
}
