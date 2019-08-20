inThisBuild(
  List(
    organization := "org.tmt",
    homepage := Some(url("https://github.com/bilal-fazlani/embedded-keycloak")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer("tmtsoftware", "TMT", "", url("https://github.com/tmtsoftware"))
    ),
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-Xlint",
      "-Ywarn-dead-code"
    )
  )
)

lazy val `embedded-keycloak` = project.settings(
  name := "embedded-keycloak",
  scalaVersion := "2.13.0",
  crossScalaVersions := List("2.12.8", "2.13.0"),
  version := {
    sys.env.get("CI") match {
      case Some("true") => version.value
      case _            => "0.1-SNAPSHOT"
    }
  },
  libraryDependencies ++= Seq(
    //  "com.softwaremill.retry" %% "retry"    % "0.3.2",
    "com.lihaoyi" %% "requests" % "0.2.0",
    "com.lihaoyi" %% "os-lib" % "0.3.0",
    "com.lihaoyi" %% "upickle" % "0.7.5",
    "com.lihaoyi" %% "ujson" % "0.7.5",
    "com.iheart" %% "ficus" % "1.4.7",
    //AKKA-DOWNLOADER
    "com.typesafe.akka" %% "akka-http" % "10.1.8",
    "com.typesafe.akka" %% "akka-stream" % "2.5.25",
    //TEST
    "org.scalatest" %% "scalatest" % "3.0.8" % Test
  ),
  parallelExecution in Test in ThisBuild := false
)
