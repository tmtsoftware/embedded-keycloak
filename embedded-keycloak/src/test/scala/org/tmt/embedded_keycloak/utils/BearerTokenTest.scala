package org.tmt.embedded_keycloak.utils

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.tmt.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationDouble

class BearerTokenTest extends AnyFunSuite with Matchers with BeforeAndAfterEach {
  test("can   login admin") {
    val settings     = Settings.default.copy(port = 9005)
    val keycloakData = KeycloakData.fromConfig
    val keycloak     = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle   = Await.result(keycloak.startServer(), 2.minutes)

    val token = BearerToken.fromServer(9005, "admin", "admin")
    token.header shouldBe defined
    stopHandle.stop()
  }

  test("can login application user") {
    val settings     = Settings.default.copy(port = 9005)
    val keycloakData = KeycloakData.fromConfig
    val keycloak     = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle   = Await.result(keycloak.startServer(), 2.minutes)

    try {
      val token =
        BearerToken.fromServer(9005, "user1", "abcd", "example-realm", "some-client")
      token.header shouldBe defined
    }
    finally {
      stopHandle.stop()
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }
}
