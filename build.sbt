name := "embedded-keycloak"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.softwaremill.retry" %% "retry" % "0.3.0",
  "com.lihaoyi" %% "os-lib" % "0.2.5",
  "org.backuity.clist" %% "clist-core"   % "3.4.0",
  "org.backuity.clist" %% "clist-macros" % "3.4.0" % "provided",
  "com.iheart" %% "ficus" % "1.4.3",
  //TEST
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)