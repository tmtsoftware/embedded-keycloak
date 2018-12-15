package tech.bilal.embedded_keycloak

case class Settings(port: Int,
                    host: String,
                    installationDirectory: String,
                    cleanPreviousData: Boolean,
                    alwaysDownload: Boolean,
                    version: String)

object Settings {
  val default =
    Settings(port = 8001,
             host = "0.0.0.0",
             installationDirectory = "/tmp/keycloak-installation/",
             cleanPreviousData = true,
             alwaysDownload = false,
             version = "4.6.0")
}
