package embedded.keycloak.data

import akka.actor.ActorSystem
import embedded.keycloak.internal.{EmbeddedKeycloak, Ports}
import embedded.keycloak.models.{KeycloakData, Settings}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble

class DataIntegrityTest extends FunSuite with Matchers with BeforeAndAfterAll {
  test("test") {
    implicit val actorSystem = ActorSystem()
    implicit val ec = actorSystem.dispatcher
    val settings = Settings.default.copy(port = 9005, version = "4.6.0")
    val keycloakData = KeycloakData.fromConfig
    val keycloak = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle = Await.result(keycloak.startServerInBackground(), 2.minutes)

    val feeder = new DataFeeder(settings, keycloakData)
    feeder.feedRealms
    stopHandle.stop()
  }

  override def afterAll(): Unit = {
    new Ports().stop(9005)
  }
}
