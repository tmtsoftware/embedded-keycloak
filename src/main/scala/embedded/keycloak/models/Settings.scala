package embedded.keycloak.models

case class Settings(port: Int,
                    host: String,
                    username: String,
                    password: String,
                    installationDirectory: String,
                    cleanInstall: Boolean,
                    version: String)

object Settings {
  val default =
    Settings(port = 8001,
             host = "0.0.0.0",
             username = "admin",
             password = "admin",
             installationDirectory = "/tmp/keycloak-installation/",
             cleanInstall = false,
             version = "4.6.0")
}
