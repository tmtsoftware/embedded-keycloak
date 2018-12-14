package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.models.Settings
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, Future}

class BlockingServerProcessTest extends FunSuite with Matchers {
  test(
    "startServer should start the keycloak server on " +
      "calling thread and should keep running until aborted") {
    val settings = Settings.default.copy(port = 9005, version = "4.7.0")
    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    val keycloak = new EmbeddedKeycloak(settings)
    val serverF = Future {
      scala.concurrent.blocking {
        keycloak.startServer()
      }
    }
    val healthCheck = new HealthCheck(settings)

    Await.result(healthCheck.checkHealth(), 1.minute)

    actorSystem.terminate()

    val ports = new Ports()
    ports.stop(settings.port)
    ports.checkAvailability(settings.port) shouldBe true
  }
}
