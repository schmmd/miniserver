name := "miniserver"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.1"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
    "net.databinder" %% "unfiltered-filter" % "0.6.8",
    "net.databinder" %% "unfiltered-jetty" % "0.6.8",
    "com.jsuereth" %% "scala-arm" % "1.3",
    "com.github.scopt" %% "scopt" % "2.1.0",
    "ch.qos.logback" % "logback-classic" % "1.0.11",
    "ch.qos.logback" % "logback-core" % "1.0.11",
    "org.slf4j" % "slf4j-api" % "1.7.1"
)
