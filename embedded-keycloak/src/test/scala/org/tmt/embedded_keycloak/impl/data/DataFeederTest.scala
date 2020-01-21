package org.tmt.embedded_keycloak.impl.data

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import org.tmt.embedded_keycloak.KeycloakData.{ApplicationUser, ClientRole}
import org.tmt.embedded_keycloak.utils.Ports
import org.tmt.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationDouble

class DataFeederTest extends FunSuite with Matchers with BeforeAndAfterAll {
  test("test data integration") {

    val settings     = Settings.default.copy(port = 9005)
    val keycloakData = KeycloakData.fromConfig
    val keycloak     = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle   = Await.result(keycloak.startServer(), 2.minutes)

    val actualRealms =
      KeycloakData.fromServer(settings, "admin", "admin").realms

    val mayBeActualRealm = actualRealms.find(x => x.name == "example-realm")

    mayBeActualRealm should not be empty

    mayBeActualRealm.foreach { actualRealm =>
      import actualRealm._

      name shouldBe "example-realm"
      realmRoles should contain allElementsOf Seq("super-admin")

      users should contain allElementsOf Set(
        ApplicationUser(
          username = "user1",
          password = "[HIDDEN]",
          firstName = "john",
          realmRoles = Set("super-admin", "uma_authorization", "offline_access"),
          clientRoles = Set(ClientRole("${client_account}", "view-profile"), ClientRole("${client_account}", "manage-account"))
        ),
        ApplicationUser(
          "user2",
          "[HIDDEN]",
          realmRoles = Set("uma_authorization", "offline_access"),
          clientRoles = Set(
            ClientRole(clientName = "some-server", roleName = "server-user"),
            ClientRole("${client_account}", "view-profile"),
            ClientRole("${client_account}", "manage-account")
          )
        )
      )

      clients.find(c => {
        c.name == "some-server" &&
        !c.authorizationEnabled &&
        c.clientRoles.contains("server-admin") &&
        c.clientRoles.contains("server-user")
      }) should not be empty

      clients.find(c => {
        c.name == "some-client" &&
        !c.authorizationEnabled
      }) should not be empty

      stopHandle.stop()
    }
  }

  override def afterAll(): Unit = {
    Ports.stop(9005)
  }
}
