inThisBuild(
  List(
    scalaVersion := "2.13.8",
    version := "0.1.0-SNAPSHOT",
    organization := "com.github.tmtsoftware.embedded-keycloak",
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

lazy val `embedded-keycloak` = (project in file("embedded-keycloak"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"       %% "requests"    % "0.7.0",
      "com.lihaoyi"       %% "os-lib"      % "0.8.0",
      "com.lihaoyi"       %% "upickle"     % "1.4.4",
      "com.lihaoyi"       %% "ujson"       % "1.4.4",
      "com.iheart"        %% "ficus"       % "1.5.1",
      //AKKA-DOWNLOADER
      "com.typesafe.akka" %% "akka-http"   % "10.2.7",
      "com.typesafe.akka" %% "akka-stream" % "2.6.18",
      //TEST
      "org.scalatest"     %% "scalatest"   % "3.2.10" % Test
    ),
    ThisBuild / Test / parallelExecution := false
  )
