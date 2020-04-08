package org.tmt.embedded_keycloak

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

    val process = os
      .proc(
        "sh",
        fileIO.keycloakExecutablePath.toString,
        s"-Djboss.bind.address=$host",
        s"-Djboss.http.port=$port"
      )
      .spawn(
        stdout = processLogger,
        stderr = processLogger
      )

    val stopHandle = new StopHandle(process, port)

    healthCheck
      .checkHealth()
      .map(_ => dataFeeder.feed(keycloakData))
      .map(_ => stopHandle)
  }
}
