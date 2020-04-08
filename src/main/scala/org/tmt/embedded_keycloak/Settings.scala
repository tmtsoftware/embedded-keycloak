package org.tmt.embedded_keycloak

case class Settings(
    port: Int = 8081,
    host: String = "0.0.0.0",
    keycloakDirectory: String = System.getProperty("user.home") + "/embedded-keycloak/",
    cleanPreviousData: Boolean = true,
    alwaysDownload: Boolean = false,
    version: String = "8.0.1",
    printProcessLogs: Boolean = true
)

object Settings {
  val default: Settings = Settings()
}
