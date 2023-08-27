package org.tmt.embedded_keycloak

import org.tmt.embedded_keycloak.impl.Bash.spawn
import org.tmt.embedded_keycloak.impl._
import org.tmt.embedded_keycloak.utils.Ports

import scala.concurrent.{ExecutionContext, Future}

class EmbeddedKeycloak(keycloakData: KeycloakData, settings: Settings = Settings.default) {

  private val wiring = new Wiring(settings)
  import settings._
  import wiring._

  private def preRun(): Unit = {
    installer.install()
    if (!Ports.isFree(port)) throw new RuntimeException(s"Failed to start keycloak server, $port is not available")
  }

  def startServer()(implicit ec: ExecutionContext): Future[StopHandle] = {
    preRun()
    val env = Map("KEYCLOAK_ADMIN" -> "admin", "KEYCLOAK_ADMIN_PASSWORD" -> "admin")
    val process = spawn(
      stdOutLogger,
      env,
      "bash",
      fileIO.keycloakExecutablePath.toString,
      "start-dev",
      s"--http-host=$host",
      s"--http-port=$port",
      s"--http-enabled=true",
    )

    val stopHandle = new StopHandle(process)

    val healthCheckResult = healthCheck.keycloakHealth()
    healthCheckResult.onComplete(_ => wiring.actorSystem.terminate())

    healthCheckResult
      .map(_ => dataFeeder.feed(keycloakData))
      .map(_ => stopHandle)
  }
}
