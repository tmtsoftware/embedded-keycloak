package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.models.Settings
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class KeycloakStartAndStopTest extends AsyncFunSuite with Matchers {
  test("testStop") {
    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    val settings = Settings.default.copy(port = 9005, version = "4.7.0")
    val keycloak = new EmbeddedKeycloak(settings)
    val stopHandle = Await.result(keycloak.startServerInBackground(), 2.minutes)

    val healthCheck = new HealthCheck(settings)

    healthCheck.checkHealth().map { _ =>
      stopHandle.stop()
      new Ports().checkAvailability(settings.port, `throw` = true) shouldBe true
    }
  }
}
