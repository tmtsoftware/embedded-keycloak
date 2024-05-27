package org.tmt.embedded_keycloak

import os.{Inherit, Pipe, ProcessOutput}

case class InvalidVersion(version: String) extends Exception(s"Unable to parse $version, first part of version number is not Int")

case class Settings(
    port: Int = 8081,
    host: String = "0.0.0.0",
    keycloakDirectory: String = System.getProperty("user.home") + "/embedded-keycloak/",
    cleanPreviousData: Boolean = true,
    alwaysDownload: Boolean = false,
    version: String = "24.0.4",
    printProcessLogs: Boolean = true
) {
  val stdOutLogger: ProcessOutput = if (printProcessLogs) Inherit else Pipe
  val keycloakDownloadUrl: String = s"https://github.com/keycloak/keycloak/releases/download/$version/keycloak-$version.tar.gz"
}

object Settings {
  val default: Settings = Settings()
}
