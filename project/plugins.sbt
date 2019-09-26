addSbtPlugin("org.scoverage"    % "sbt-scoverage"             % "1.6.0")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"             % "1.2.7")
addSbtPlugin("com.geirsson"     % "sbt-ci-release"            % "1.2.6")
addSbtPlugin("com.codacy"       % "sbt-codacy-coverage"       % "2.112")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.4.1")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.11")

addSbtCoursier

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
