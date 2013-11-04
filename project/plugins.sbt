//webplugin
resolvers += "Sonatype Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.4.2")

resolvers += "sbt-idea-repo" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.openstudy" %% "sbt-resource-management" % "0.4.1-SNAPSHOT")
