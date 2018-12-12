package embedded.keycloak.internal

import embedded.keycloak.internal.Extensions.RichProc
import embedded.keycloak.models.Settings
import os.Path
import Bash._

import scala.concurrent.{ExecutionContext, Future}

class EmbeddedKeycloak(settings: Settings) {

  val installer = new Installer(settings)

  import settings._

  private def getBinDirectory =
    Path(installationDirectory) / version / s"binaries" / s"keycloak-$version.Final" / "bin"

  def startServer()(implicit ec: ExecutionContext): Future[Unit] = {
    installer.install().map { _ =>
      //bin/standalone.sh -Djboss.bind.address=${host}
      // //-Djboss.http.port=${port}
      // //-Dkeycloak.migration.action=import
      // //-Dkeycloak.migration.provider=singleFile
      // -Dkeycloak.migration.file=$path" -p "$port"

      exec(
        s"sh ${getBinDirectory / "standalone.sh"} " +
          s"-Djboss.bind.address=$host " +
          s"-Djboss.http.port=$port")
    }
  }
}
