package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.models.{KeycloakData, Settings}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class BackgroundServerProcessTest extends AsyncFunSuite with Matchers {
  test(
    "startServerInBackground should start the server as a child process" +
      " and should stop when stop method is called") {
    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    val settings = Settings.default.copy(port = 9005, version = "4.7.0")
    val keycloak = new EmbeddedKeycloak(KeycloakData.fromConfig, settings)
    val stopHandle = Await.result(keycloak.startServerInBackground(), 2.minutes)

    val healthCheck = new HealthCheck(settings)

    healthCheck.checkHealth().map { _ =>
      stopHandle.stop()
      new Ports().checkAvailability(settings.port, `throw` = true) shouldBe true
    }
  }
}
