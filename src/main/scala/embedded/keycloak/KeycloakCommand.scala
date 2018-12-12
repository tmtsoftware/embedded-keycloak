package embedded.keycloak

import embedded.keycloak.internal.EmbeddedKeycloak
import embedded.keycloak.models.Settings
import org.backuity.clist._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.StdIn

class KeycloakCommand extends Command(description = "starts keycloak server") {

  var port = opt[Int](default = 8081,
                      description =
                        "port number to use for keycloak http server")

  var host = opt[String](default = "0.0.0.0", description = "address to bind")

  var username =
    opt[String](default = "admin", description = "username of super admin")

  var password =
    opt[String](default = "admin", description = "password of super admin")

  var installationDirectory =
    opt[String](default = "/tmp/keycloak-installation/")

  var cleanInstall = opt[Boolean](
    default = false,
    abbrev = "c",
    description =
      "delete current installation if exists and installs a fresh instance")

  var version = opt[String](default = "4.6.0")

  private def settings =
    Settings(port,
             host,
             username,
             password,
             installationDirectory,
             cleanInstall,
             version)

  def run(): Unit = {
    println(s"""
         |OPTIONS:
         |
         |port: $port
         |host: $host
         |username: $username
         |password: $password
         |installationDirectory: $installationDirectory
         |cleanInstall: $cleanInstall
         |version: $version
       """.stripMargin)

    val keycloak = new EmbeddedKeycloak(settings)

    import scala.concurrent.ExecutionContext.Implicits._

    Await.result(keycloak.startServer(), 10.minutes)
    StdIn.readLine("press return to exit...")
  }
}
