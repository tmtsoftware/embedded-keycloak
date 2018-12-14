package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.models.{KeycloakData, Settings}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{Await, Future}

class BlockingServerProcessTest
    extends FunSuite
    with Matchers
    with BeforeAndAfterAll {

  val settings: Settings = Settings.default.copy(port = 9005, version = "4.7.0")
  val ports = new Ports()

  test(
    "startServer should start the keycloak server on " +
      "calling thread and should keep running until aborted") {
    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    val keycloak = new EmbeddedKeycloak(KeycloakData.fromConfig, settings)
    val serverF = Future {
      scala.concurrent.blocking {
        keycloak.startServer()
      }
    }
    val healthCheck = new HealthCheck(settings)

    Await.result(healthCheck.checkHealth(), 1.minute)

    actorSystem.terminate()

    ports.checkAvailability(settings.port) shouldBe true
  }

  override def afterAll(): Unit = ports.stop(settings.port)
}
