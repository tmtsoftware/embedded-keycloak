addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.3.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint:-unused,_",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture"
)
