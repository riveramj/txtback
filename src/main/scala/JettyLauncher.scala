import java.util
import javax.servlet.DispatcherType
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.{FilterHolder, DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import net.liftweb.http.LiftFilter


object JettyLauncher extends App {
  val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(port)
  server.setConnectors(Array(scc))

  val context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS)
  context.setSessionHandler(new SessionHandler())
  context.addServlet(classOf[DefaultServlet], "/")
  context.addFilter(classOf[LiftFilter], "/*", util.EnumSet.of(DispatcherType.REQUEST))
  context.setResourceBase("src/main/webapp")

  server.setHandler(context)
  server.start
  server.join
}