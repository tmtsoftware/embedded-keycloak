addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.6.4")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"              % "2.4.6")

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  //"-Xfatal-warnings",
  "-Xlint:-unused,_",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture"
)
