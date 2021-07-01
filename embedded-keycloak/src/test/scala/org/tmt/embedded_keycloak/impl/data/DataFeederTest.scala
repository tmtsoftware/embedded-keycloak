package org.tmt.embedded_keycloak.impl.data

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.tmt.embedded_keycloak.KeycloakData.{ApplicationUser, ClientRole}
import org.tmt.embedded_keycloak.impl.StopHandle
import org.tmt.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationDouble

class DataFeederTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {
  private var stopHandle                  = Option.empty[StopHandle]
  override protected def afterAll(): Unit = stopHandle.foreach(_.stop())

  test("test data integration") {

    val settings     = Settings.default.copy(port = 9005)
    val keycloakData = KeycloakData.fromConfig
    val keycloak     = new EmbeddedKeycloak(keycloakData, settings)
    stopHandle = Some(Await.result(keycloak.startServer(), 2.minutes))

    val actualRealms =
      KeycloakData.fromServer(settings, "admin", "admin").realms

    val mayBeActualRealm = actualRealms.find(_.name == "example-realm")

    mayBeActualRealm shouldBe defined

    mayBeActualRealm.foreach { actualRealm =>
      import actualRealm._

      name shouldBe "example-realm"
      realmRoles should contain allElementsOf Seq("super-admin")

      users should contain allElementsOf Set(
        ApplicationUser(
          username = "user1",
          password = "[HIDDEN]",
          firstName = "john",
          realmRoles = Set("super-admin", "default-roles-example-realm")
        ),
        ApplicationUser(
          "user2",
          "[HIDDEN]",
          realmRoles = Set("default-roles-example-realm"),
          clientRoles = Set(ClientRole(clientName = "some-server", roleName = "server-user"))
        )
      )

      clients.find { c =>
        c.name == "some-server" &&
        !c.authorizationEnabled &&
        c.clientRoles.contains("server-admin") &&
        c.clientRoles.contains("server-user")
      } should not be empty

      clients.find { c => c.name == "some-client" && !c.authorizationEnabled } should not be empty
    }
  }
}
