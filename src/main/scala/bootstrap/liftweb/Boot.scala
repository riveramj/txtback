package bootstrap.liftweb

import net.liftweb._
import util._

import common._
import http._
import sitemap._
import net.liftweb.http.js.jquery._
import net.liftweb.http.Html5Properties
import com.riveramj.util.Paths
import com.riveramj.service.{SurveyService, SurveyorService}
import com.riveramj.util.TestDataLoader
import com.riveramj.api.NewMessageListener

class Boot extends Loggable {
  def boot {

    MongoConfig.init
    MailConfig.init

    // where to search snippet
    LiftRules.addToPackages("com.riveramj")

    //    // Build SiteMap
    //    val entries = List(
      //      Menu.i("Index") / "index"
      //    )
      //
      //    // set the sitemap.  Note if you don't want access control for
      //    // each page, just comment this line out.
      //    LiftRules.setSiteMap(SiteMap(entries:_*))
      //

    LiftRules.setSiteMap(Paths.siteMap)
    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
    Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
    Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // set DocType to HTML5
    LiftRules.htmlProperties.default.set((r: Req) =>new Html5Properties(r.userAgent))

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts

    // stateless -- no session created
    LiftRules.statelessDispatch.append(NewMessageListener)

    SurveyorService.getAllUsers match {
      case users if users.isEmpty => TestDataLoader.createTestUsers()
      case _ =>
    }

    if(Props.mode == Props.RunModes.Development || Props.mode == Props.RunModes.Pilot)
    SurveyService.getAllSurveys match {
      case surveys if surveys.isEmpty => TestDataLoader.createTestQuestions()
      case _ =>
    }
  } //boot
} //Boot

