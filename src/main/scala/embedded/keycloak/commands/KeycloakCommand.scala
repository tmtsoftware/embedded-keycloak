package embedded.keycloak.commands

import akka.actor.ActorSystem
import embedded.keycloak.internal.EmbeddedKeycloak
import embedded.keycloak.models.{KeycloakData, Settings}
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

  var cleanPreviousData: Boolean = opt[Boolean](
    default = default.cleanPreviousData,
    abbrev = "c",
    description =
      "delete current date if exists and start with a fresh instance")

  var alwaysDownload: Boolean = opt[Boolean](
    default = default.alwaysDownload,
    abbrev = "d",
    description = "downloads keycloak again (results in slow start)")

  var version: String = opt[String](default = default.version)

  private def settings =
    Settings(port,
             host,
             installationDirectory,
             cleanPreviousData,
             alwaysDownload,
             version)

  def run(): Unit = {
    println(s"""
         |OPTIONS:
         |
         |port: $port
         |host: $host
         |installationDirectory: $installationDirectory
         |cleanPreviousData: $cleanPreviousData
         |alwaysDownload: $alwaysDownload
         |version: $version
       """.stripMargin)

    import scala.concurrent.ExecutionContext.Implicits._

    implicit val actorSystem: ActorSystem = ActorSystem()

    val embeddedKeycloak =
      new EmbeddedKeycloak(KeycloakData.fromConfig, settings)

    embeddedKeycloak.startServer()
  }
}
