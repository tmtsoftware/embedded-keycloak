inThisBuild(List(
  organization := "org.tmt",
  homepage := Some(url("https://github.com/bilal-fazlani/embedded-keycloak")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "tmtsoftware",
      "TMT",
      "",
      url("https://github.com/tmtsoftware")
    )
  ),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-deprecation",
    //"-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Xfuture"
  )

))

name := "embedded-keycloak"

scalaVersion := "2.12.8"

version := {
  sys.env.get("CI") match {
    case Some("true") => version.value
    case _            => "0.1-SNAPSHOT"
  }
}

libraryDependencies ++= Seq(
  "com.softwaremill.retry" %% "retry" % "0.3.2",
  "com.lihaoyi" %% "requests" % "0.1.8",
  "com.lihaoyi" %% "os-lib" % "0.2.9",
  "com.lihaoyi" %% "upickle" % "0.7.4",
  "com.lihaoyi" %% "ujson" % "0.7.4",
  "com.iheart" %% "ficus" % "1.4.5",
  //AKKA-DOWNLOADER
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-stream" % "2.5.22",
  //TEST
  "org.scalatest" %% "scalatest" % "3.0.6" % Test
)

parallelExecution in Test in ThisBuild := false

