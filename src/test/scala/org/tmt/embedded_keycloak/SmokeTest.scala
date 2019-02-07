package org.tmt.embedded_keycloak

import org.scalatest.{AsyncFunSuite, Matchers}
import org.tmt.embedded_keycloak.impl.HealthCheck
import org.tmt.embedded_keycloak.utils.Ports

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class SmokeTest extends AsyncFunSuite with Matchers {
  test(
    "startServer should start the server as a child process" +
      " and should stop when stop method is called") {

    val settings = Settings.default.copy(port = 9005, version = "4.6.0")
    val keycloak = new EmbeddedKeycloak(KeycloakData.fromConfig, settings)
    implicit val ec = scala.concurrent.ExecutionContext.global
    val stopHandle = Await.result(keycloak.startServer(), 5.minutes)

    val healthCheck = new HealthCheck(settings)

    healthCheck.checkHealth().map { _ =>
      stopHandle.stop()
      new Ports()
        .checkAvailability(settings.port, throwOnError = true) shouldBe true
    }
  }
}
