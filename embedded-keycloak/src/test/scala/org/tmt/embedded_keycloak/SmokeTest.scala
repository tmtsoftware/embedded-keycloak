package org.tmt.embedded_keycloak

import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import org.tmt.embedded_keycloak.utils.Ports

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class SmokeTest extends FunSuite with Matchers with BeforeAndAfterAll {

  test("startServer should start the server as a child process and should stop when stop method is called") {

    val settings   = Settings.default.copy(port = 9005)
    val keycloak   = new EmbeddedKeycloak(KeycloakData.fromConfig, settings)
    val stopHandle = Await.result(keycloak.startServer(), 5.minutes)

    stopHandle.stop()

    Eventually.eventually(Ports.checkAvailability(settings.port) shouldBe true)
  }

  override def afterAll(): Unit = Ports.stop(9005)
}
