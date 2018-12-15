package tech.bilal.embedded_keycloak.impl.data

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import tech.bilal.embedded_keycloak.impl.Ports
import tech.bilal.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble

class DataFeederTest extends FunSuite with Matchers with BeforeAndAfterAll {
  test("test") {

    val settings = Settings.default.copy(port = 9005, version = "4.6.0")
    val keycloakData = KeycloakData.fromConfig
    val keycloak = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle = Await.result(keycloak.startServerInBackground(), 2.minutes)

    //todo: assert the data here somehow

    stopHandle.stop()
  }

  override def afterAll(): Unit = {
    new Ports().stop(9005)
  }
}
