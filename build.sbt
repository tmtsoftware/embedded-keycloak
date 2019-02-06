inThisBuild(List(
  organization := "tech.bilal",
  homepage := Some(url("https://github.com/bilal-fazlani/embedded-keycloak")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "bilal-fazlani",
      "Bilal Fazlani",
      "bilal.m.fazlani@gmail.com",
      url("https://github.com/bilal-fazlani")
    )
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
  "com.softwaremill.retry" %% "retry" % "0.3.1",
  "com.lihaoyi" %% "requests" % "0.1.7",
  "com.lihaoyi" %% "os-lib" % "0.2.6",
  "com.lihaoyi" %% "upickle" % "0.7.1",
  "com.lihaoyi" %% "ujson" % "0.7.1",
  "com.iheart" %% "ficus" % "1.4.4",
  //AKKA-DOWNLOADER
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  //TEST
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

parallelExecution in Test in ThisBuild := false

