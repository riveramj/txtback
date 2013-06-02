package com.riveramj.api

import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.json.JsonAST.{JString, JValue}

object NewMessageListener extends RestHelper with Loggable {

  serve("api" / "textMessage" prefix {
    case "newMessage"  :: _ Post req =>
    {
      println("======= " + req._params)
      JString("ok")

    }
  })

}