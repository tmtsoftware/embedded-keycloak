package embedded.keycloak.internal

import embedded.keycloak.models.Settings
import os.Path

import scala.concurrent.{ExecutionContext, Future}

class EmbeddedKeycloak(settings: Settings) {

  val installer = new Installer(settings)

  import settings._

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  def startServer()(implicit ec: ExecutionContext): Future[Unit] = {
    installer.install(x => print(s"\r$x")).map { _ =>
      //bin/standalone.sh -Djboss.bind.address=${host}
      // //-Djboss.http.port=${port}
      // //-Dkeycloak.migration.action=import
      // //-Dkeycloak.migration.provider=singleFile
      // -Dkeycloak.migration.file=$path" -p "$port"
      val exitCode = os
        .proc("sh",
              getBinDirectory / "standalone.sh",
              s"-Djboss.bind.address=$host",
              s"-Djboss.http.port=$port")
        .stream(
          onOut = (buffer, length) =>
            buffer
              .slice(0, length)
              .map(x => x.toChar)
              .mkString
              .split("\n")
              .foreach(println),
          onErr = (buffer, length) =>
            buffer
              .slice(0, length)
              .map(x => x.toChar)
              .mkString
              .split("\n")
              .foreach(println)
        )

      if (exitCode != 0)
        throw new RuntimeException(
          "could not start keycloak server. " +
            s"exit code $exitCode")
    }
  }
}
