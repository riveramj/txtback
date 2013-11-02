import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name := "TxtBck"

version := "0.1-SNAPSHOT"

organization := "com.riveramj"

scalaVersion := "2.10.2"

seq(webSettings :_*)

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")

libraryDependencies ++= {
val liftVersion = "2.5.1"
  Seq(
    "ch.qos.logback"         %  "logback-classic"      % "0.9.26",
    "org.apache.shiro"       %  "shiro-core"           % "1.2.0",
    "org.eclipse.jetty"      %  "jetty-webapp"         % "8.0.1.v20110908" % "container; compile->default",
    "com.twilio.sdk"         % "twilio-java-sdk"       % "3.3.15"          % "compile",
    "net.liftweb"            %% "lift-json"            % liftVersion       % "compile->default",
    "net.liftweb"            %% "lift-mongodb"         % liftVersion       % "compile->default",
    "net.liftweb"            %% "lift-webkit"          % liftVersion       % "compile",
    "net.liftweb"            %% "lift-mapper"          % liftVersion       % "compile",
    "org.specs2"             %% "specs2"               % "1.14"            % "test"
  )
}

parallelExecution in Test := false

// append -deprecation to the options passed to the Scala compiler
scalacOptions += "-deprecation"

