name := "embedded-keycloak"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.softwaremill.retry" %% "retry" % "0.3.0",
  "com.lihaoyi" %% "requests" % "0.1.4",
  "com.lihaoyi" %% "os-lib" % "0.2.5",
  "com.lihaoyi" %% "upickle" % "0.7.1",
  "com.lihaoyi" %% "ujson" % "0.7.1",
  "com.iheart" %% "ficus" % "1.4.3",
  //TEST
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

parallelExecution in Test in ThisBuild := false