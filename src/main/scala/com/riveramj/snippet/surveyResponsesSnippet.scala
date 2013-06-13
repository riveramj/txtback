package com.riveramj.snippet

import net.liftweb.sitemap.{*, Menu}
import net.liftweb.common.Full
import net.liftweb.sitemap.Loc.TemplateBox
import net.liftweb.http.Templates

object SurveyResponsesSnippet {
  lazy val menu = Menu.param[String]("responses","responses",
    Full(_),
    (id) => id
  ) / "survey" / * / "responses" / * >>
    //loggedIn >>
    TemplateBox(() => Templates( "survey" :: "responses" :: Nil))
}

class SurveyResponsesSnippet {

}
