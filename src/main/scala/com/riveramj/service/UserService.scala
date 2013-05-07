package com.riveramj.service

import org.apache.shiro.crypto.SecureRandomNumberGenerator
import com.riveramj.model.User
import com.riveramj.util.RandomIdGenerator._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.mapper.By
import org.apache.shiro.crypto.hash.Sha256Hash

object UserService extends Loggable {
  private val rng = new SecureRandomNumberGenerator();

  def hashPassword(password: String, salt: String) : String = {
    new Sha256Hash(password, salt, 1024).toBase64;
  }

  def getSalt : String = {
    rng.nextBytes(32).toBase64;
  }

  def createUser(
                  firstName: String, lastName: String, email: String, 
                  companyId: Long, password: String) = {

    val salt = getSalt
    val hashedPassword = hashPassword(password, salt)
    
    val user = User.create
      .firstName(firstName)
      .lastName(lastName)
      .email(email)
      .companyId(companyId)
      .password(hashedPassword)
      .salt(salt)

    tryo(saveUser(user)) flatMap {
      u => u match {
        case Full(newUser:User) => Full(newUser)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def saveUser(user:User):Box[User] = {

    val uniqueConstraintPattern = """.*Unique(.+)""".r
    val validateErrors = user.validate

    if (validateErrors.isEmpty) {
      tryo(user.saveMe()) match {
        case Full(newUser:User) => Full(newUser)
        case Failure(_, Full(err), _) => {
          val error = err.getMessage.substring(0, err.getMessage.indexOf("\n"))
          error match {
            case uniqueConstraintPattern(x) => Failure("User Already Exists")
            case _ => Failure("Unknown error")
          }
        }
        case _ => Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getUserById(userId: Long): Box[User] = {
    User.find(By(User.id, userId))
  }

  def deleteUserById(userId: Long): Box[Boolean] = {
    val user = User.find(By(User.id, userId))
    user.map(_.delete_!)
  }

  def getUserByName(email: String): Box[User] = {
    User.find(By(User.email, email))
  }

  def getAllCompanies = {
    User.findAll()
  }

}
