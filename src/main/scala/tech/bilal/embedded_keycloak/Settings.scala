package tech.bilal.embedded_keycloak

case class Settings(port: Int = 8081,
                    host: String = "0.0.0.0",
                    installationDirectory: String =
                      "/tmp/keycloak-installation/",
                    cleanPreviousData: Boolean = true,
                    alwaysDownload: Boolean = false,
                    version: String = "4.6.0")

object Settings {
  val default = Settings()
}
