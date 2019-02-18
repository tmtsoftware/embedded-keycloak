addSbtPlugin("org.scoverage"    % "sbt-scoverage"             % "1.5.1")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"             % "1.2.7")
addSbtPlugin("com.geirsson"     % "sbt-ci-release"            % "1.2.1")
addSbtPlugin("com.codacy"       % "sbt-codacy-coverage"       % "2.112")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.4.0")
addSbtPlugin("com.orrsella"     % "sbt-stats"                 % "1.0.7")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.8")

addSbtCoursier

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  //"-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture"
)
