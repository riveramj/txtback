package bootstrap.liftweb

import _root_.com.riveramj.db.C3P0DBVendor
import net.liftweb._
import util._

import common._
import http._
import sitemap._
import net.liftweb.http.js.jquery._
import net.liftweb.mapper._
import net.liftweb.http.Html5Properties
import net.liftweb.mapper.Schemifier
import com.riveramj.model._
import com.riveramj.util.Paths
import com.riveramj.service.SurveyorService
import com.riveramj.util.TestDataLoader


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {

    if (!DB.jndiJdbcConnAvailable_?) {
      LiftRules.unloadHooks.append(C3P0DBVendor.shutDown _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, C3P0DBVendor)

    }

    Schemifier.schemify(
      true,
      Schemifier.infoF
      _,
      Question,
      Survey,
      Company,
      Surveyor
    )

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

    SurveyorService.getAllUsers match {
      case users if users.isEmpty => TestDataLoader.createTestData()
      case _ =>
    }

  } //boot

} //Boot

