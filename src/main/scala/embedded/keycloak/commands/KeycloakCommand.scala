package embedded.keycloak.commands

import akka.actor.ActorSystem
import embedded.keycloak.internal.EmbeddedKeycloak
import embedded.keycloak.models.Settings
import org.backuity.clist._

class KeycloakCommand extends Command(description = "starts keycloak server") {

  import Settings.default

  var port: Int = opt[Int](default = 8081,
                           description =
                             "port number to use for keycloak http server")

  var host: String =
    opt[String](default = default.host, description = "address to bind")

  var installationDirectory: String =
    opt[String](default = default.installationDirectory)

  var cleanInstall: Boolean = opt[Boolean](
    default = default.cleanInstall,
    abbrev = "c",
    description =
      "delete current installation if exists and installs a fresh instance")

  var version: String = opt[String](default = default.version)

  private def settings =
    Settings(port, host, installationDirectory, cleanInstall, version)

  def run(): Unit = {
    println(s"""
         |OPTIONS:
         |
         |port: $port
         |host: $host
         |installationDirectory: $installationDirectory
         |cleanInstall: $cleanInstall
         |version: $version
       """.stripMargin)

    import scala.concurrent.ExecutionContext.Implicits._

    implicit val actorSystem: ActorSystem = ActorSystem()

    val embeddedKeycloak =
      new EmbeddedKeycloak(settings)

    embeddedKeycloak.startServer()
  }
}
