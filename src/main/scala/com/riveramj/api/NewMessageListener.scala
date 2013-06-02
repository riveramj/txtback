package com.riveramj.api

import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.json.JsonAST.{JString, JValue}

object NewMessageListener extends RestHelper with Loggable {

  serve("api" / "textMessage" prefix {
    case "newMessage"  :: _ Post req =>
    {
      println("======= " + req.paramNames)
      println("======= " + req.param("Body"))

      val fromPhone = req.param("From")
      val msg = req.param("Body")

      println("body is %s and its from %s" format(msg,fromPhone))

      JString("ok")

    }
  })

}