package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.internal.Bash._
import embedded.keycloak.models.{Settings, StopHandle}
import os.Path

import scala.concurrent.{ExecutionContext, Future}

class EmbeddedKeycloak(settings: Settings = Settings.default)(
    implicit actorSystem: ActorSystem) {

  private val installer = new Installer(settings)
  private val healthCheck = new HealthCheck(settings)
  private val ports = new Ports()

  import settings._

  private def getBinDirectory =
    Path(installationDirectory) / version / s"binaries" / s"keycloak-$version.Final" / "bin"

  def preRun(): Unit = {
    installer.install()
    ports.checkAvailability(port = port, `throw` = true)
  }

  def startServer()(implicit ec: ExecutionContext): Unit = {
    preRun()
    exec(
      s"sh ${getBinDirectory / "standalone.sh"} " +
        s"-Djboss.bind.address=$host " +
        s"-Djboss.http.port=$port")
  }

  def startServerInBackground()(
      implicit ec: ExecutionContext): Future[StopHandle] = {
    preRun()

    val process = background(
      s"sh ${getBinDirectory / "standalone.sh"} " +
        s"-Djboss.bind.address=$host " +
        s"-Djboss.http.port=$port")

    val stopHandle = new StopHandle(process)

    healthCheck.checkHealth().map(_ => stopHandle)
  }
}
