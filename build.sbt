inThisBuild(
  Seq(
    scalaVersion := "3.3.0",
    version := "0.6.0-pekko",
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
//      "-source:3.0-migration",
//      "-rewrite",
//      "-explain",
//      "-explain-types"
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
      "com.lihaoyi" %% "requests" % "0.8.0",
      "com.lihaoyi" %% "os-lib" % "0.9.1",
      "com.lihaoyi" %% "upickle" % "3.1.2",
      "com.lihaoyi" %% "ujson" % "3.1.2",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.4",
      "org.apache.pekko" %% "pekko-http"   % "1.0.0-RC2",
      "org.apache.pekko" %% "pekko-stream" % "1.0.1",
      //TEST
      "org.scalatest" %% "scalatest" % "3.2.16" % Test
    ),
    ThisBuild / Test / parallelExecution := false
  )
