inThisBuild(
  List(
    scalaVersion := "2.13.1",
    organization := "org.tmt",
    homepage := Some(url("https://github.com/tmtsoftware/embedded-keycloak")),
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
  version := {
    sys.env.get("CI") match {
      case Some("true") => version.value
      case _            => "0.1.0-SNAPSHOT"
    }
  },
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "requests" % "0.5.0",
    "com.lihaoyi" %% "os-lib"   % "0.6.3",
    "com.lihaoyi" %% "upickle"  % "0.9.8",
    "com.lihaoyi" %% "ujson"    % "0.9.8",
    "com.iheart"  %% "ficus"    % "1.4.7",
    //AKKA-DOWNLOADER
    "com.typesafe.akka" %% "akka-http"   % "10.1.11",
    "com.typesafe.akka" %% "akka-stream" % "2.6.2",
    //TEST
    "org.scalatest" %% "scalatest" % "3.0.8" % Test
  ),
  parallelExecution in Test in ThisBuild := false
)
