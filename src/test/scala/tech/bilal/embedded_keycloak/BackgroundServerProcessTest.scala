package tech.bilal.embedded_keycloak

import org.scalatest.{AsyncFunSuite, Matchers}
import tech.bilal.embedded_keycloak.impl.{HealthCheck, Ports}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BackgroundServerProcessTest extends AsyncFunSuite with Matchers {
  test(
    "startServerInBackground should start the server as a child process" +
      " and should stop when stop method is called") {

    val settings = Settings.default.copy(port = 9005, version = "4.7.0")
    val keycloak = new EmbeddedKeycloak(KeycloakData.fromConfig, settings)
    implicit val ec = scala.concurrent.ExecutionContext.global
    val stopHandle = Await.result(keycloak.startServerInBackground(), 5.minutes)

    val healthCheck = new HealthCheck(settings)

    healthCheck.checkHealth().map { _ =>
      stopHandle.stop()
      new Ports().checkAvailability(settings.port, `throw` = true) shouldBe true
    }
  }
}
