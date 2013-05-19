package com.riveramj.service

import org.apache.shiro.crypto.SecureRandomNumberGenerator
import com.riveramj.model.Surveyor
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.mapper.By
import org.apache.shiro.crypto.hash.Sha256Hash

object SurveyorService extends Loggable {

  def hashPassword(password: String, salt: String) : String = {
    new Sha256Hash(password, salt, 1024).toBase64
  }

  private def getSalt : String = {
    generateStringId
  }

  def createSurveyor(
                  firstName: String, lastName: String, email: String, 
                  companyId: Long, password: String) = {

    val salt = getSalt
    val hashedPassword = hashPassword(password, salt)
    
    val user = Surveyor.create
      .firstName(firstName)
      .lastName(lastName)
      .email(email)
      .companyId(companyId)
      .userId(generateLongId)
      .password(hashedPassword)
      .salt(salt)

    tryo(saveUser(user)) flatMap {
      u => u match {
        case Full(newUser:Surveyor) => Full(newUser)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def saveUser(user:Surveyor):Box[Surveyor] = {

    val uniqueConstraintPattern = """.*Unique(.+)""".r
    val validateErrors = user.validate

    if (validateErrors.isEmpty) {
      tryo(user.saveMe()) match {
        case Full(newUser:Surveyor) => Full(newUser)
        case Failure(_, Full(err), _) => {
          val error = err.getMessage.substring(0, err.getMessage.indexOf("\n"))
          error match {
            case uniqueConstraintPattern(x) => Failure("Surveyor Already Exists")
            case _ => Failure("Unknown error")
          }
        }
        case _ => Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getUserById(userId: Long): Box[Surveyor] = {
    Surveyor.find(By(Surveyor.userId, userId))
  }

  def deleteUserById(userId: Long): Box[Boolean] = {
    val user = Surveyor.find(By(Surveyor.userId, userId))
    user.map(_.delete_!)
  }

  def getUserByEmail(email: String): Box[Surveyor] = {
    Surveyor.find(By(Surveyor.email, email))
  }

  def getAllUsers = {
    Surveyor.findAll()
  }
}
