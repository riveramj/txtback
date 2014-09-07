import sbt._
import Keys._
import com.openstudy.sbt.ResourceManagementPlugin._
import com.earldouglas.xsbtwebplugin._
import AssemblyKeys._

name := "TxtBack"

version := "0.1-SNAPSHOT"

organization := "com.riveramj"

scalaVersion := "2.10.4"

seq(webSettings :_*)

assemblySettings

mainClass in assembly := Some("bootstrap.liftweb.Start")

seq(resourceManagementSettings :_*)

seq(
        targetJavaScriptDirectory in ResourceCompile <<= (PluginKeys.webappResources in Compile) apply { resources => (resources / "static" / "js").get(0) },
        scriptDirectories in ResourceCompile <<= (PluginKeys.webappResources in Compile) map { resources => (resources / "javascript").get },
        styleDirectories in ResourceCompile <<= (PluginKeys.webappResources in Compile) map { resources => (resources / "static" / "css").get },
        // This is the same as the target above. Currently in production, we don't
        // deploy compressed scripts to S3, so we need them to live in the
        // same static files directory as we put dev JS files in during
        // development.
        compressedTarget in ResourceCompile <<= (PluginKeys.webappResources in Compile) apply { resources => (resources / "static").get(0) }
      )

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")

libraryDependencies ++= {
val liftVersion = "2.6-M3"
  Seq(
    "ch.qos.logback"         %  "logback-classic"      % "1.1.2",
    "org.apache.shiro"       %  "shiro-core"           % "1.2.3",
    "org.eclipse.jetty"      %  "jetty-webapp"         % "9.2.2.v20140723" % "container,compile",
    "org.eclipse.jetty"      %  "jetty-webapp"         % "9.2.2.v20140723" % "container; compile->default",
    "com.twilio.sdk"         % "twilio-java-sdk"       % "3.4.6"          % "compile",
    "net.liftweb"            %% "lift-json"            % liftVersion       % "compile->default",
    "net.liftweb"            %% "lift-mongodb"         % liftVersion       % "compile->default",
    "net.liftweb"            %% "lift-webkit"          % liftVersion       % "compile",
    "net.liftweb"            %% "lift-mapper"          % liftVersion       % "compile",
    "org.specs2"             %% "specs2"               % "1.14"            % "test"
  )
}

/* streamlined generation of self serving embedded jetty jar */
resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map
{ (managedBase, base) =>
  val webappBase = base / "src" / "main" / "webapp"
  for {
    (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase /
      "main" / "webapp")
  } yield {
    Sync.copy(from, to)
    to
  }
}

parallelExecution in Test := false

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature")