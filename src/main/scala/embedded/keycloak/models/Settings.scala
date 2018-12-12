package embedded.keycloak.models

case class Settings(port: Int,
                    host: String,
                    username: String,
                    password: String,
                    installationDirectory: String,
                    cleanInstall: Boolean,
                    version: String)
