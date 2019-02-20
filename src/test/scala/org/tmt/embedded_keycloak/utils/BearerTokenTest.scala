package org.tmt.embedded_keycloak.utils

import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.tmt.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationDouble

class BearerTokenTest extends FunSuite with Matchers with BeforeAndAfterEach {
  test("can login admin") {
    val settings     = Settings.default.copy(port = 9005, version = "4.6.0")
    val keycloakData = KeycloakData.fromConfig
    val keycloak     = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle   = Await.result(keycloak.startServer(), 2.minutes)

    val token = BearerToken.fromServer(9005, "admin", "admin")
    token.header should not be empty
    stopHandle.stop()
  }

  test("can login application user") {
    val settings     = Settings.default.copy(port = 9005, version = "4.6.0")
    val keycloakData = KeycloakData.fromConfig
    val keycloak     = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle   = Await.result(keycloak.startServer(), 2.minutes)

    val token =
      BearerToken.fromServer(9005, "user1", "abcd", "example-realm", "some-client")
    token.header should not be empty
    stopHandle.stop()
  }

  override def beforeEach(): Unit = {
    super.afterEach()
    Ports.stop(9005)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Ports.stop(9005)
  }
}
