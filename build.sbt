inThisBuild(
  List(
    scalaVersion := "2.13.10",
    version := "0.6.0",
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
      "com.lihaoyi"       %% "requests"    % "0.7.1",
      "com.lihaoyi"       %% "os-lib"      % "0.8.1",
      "com.lihaoyi"       %% "upickle"     % "2.0.0",
      "com.lihaoyi"       %% "ujson"       % "2.0.0",
      "com.iheart"        %% "ficus"       % "1.5.2",
      //AKKA-DOWNLOADER
      "com.typesafe.akka" %% "akka-http"   % "10.4.0",
      "com.typesafe.akka" %% "akka-stream" % "2.7.0",
      //TEST
      "org.scalatest"     %% "scalatest"   % "3.2.14" % Test
    ),
    ThisBuild / Test / parallelExecution := false
  )
