inThisBuild(
  Seq(
    scalaVersion := "3.6.4",
    version := "0.7.4",
    organization := "com.github.tmtsoftware.embedded-keycloak",
    homepage := Some(url("https://github.com/tmtsoftware/embedded-keycloak")),
    resolvers += "jitpack" at "https://jitpack.io",
    resolvers += "Apache Pekko Staging".at("https://repository.apache.org/content/groups/staging"),
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
    )
  )
)

lazy val `embedded-keycloak-root` = project
  .in(file("."))
  .aggregate(
    `embedded-keycloak`
  )


lazy val `embedded-keycloak` = project.in(file("embedded-keycloak"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "requests" % "0.9.0",
      "com.lihaoyi" %% "os-lib" % "0.11.4",
      "com.lihaoyi" %% "upickle" % "4.1.0",
      "com.lihaoyi" %% "ujson" % "4.1.0",
      "org.apache.pekko" %% "pekko-http"   % "1.1.0",
      "org.apache.pekko" %% "pekko-stream" % "1.1.3",
      //TEST
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    ThisBuild / Test / parallelExecution := false
  )
