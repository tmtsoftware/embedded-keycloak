package org.tmt.embedded_keycloak

case class Settings(
    port: Int = 8081,
    host: String = "0.0.0.0",
    keycloakDirectory: String = "/tmp/embedded-keycloak/",
    cleanPreviousData: Boolean = true,
    alwaysDownload: Boolean = false,
    version: String = "5.0.0"
)

object Settings {
  val default = Settings()
}
