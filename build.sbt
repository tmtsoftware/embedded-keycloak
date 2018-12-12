name := "embedded-keycloak"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
//  "com.lihaoyi" %% "requests" % "0.1.4",
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.lihaoyi" %% "os-lib" % "0.2.5",
  "org.backuity.clist" %% "clist-core"   % "3.4.0",
  "org.backuity.clist" %% "clist-macros" % "3.4.0" % "provided")