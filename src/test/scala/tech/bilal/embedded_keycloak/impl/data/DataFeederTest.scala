package tech.bilal.embedded_keycloak.impl.data

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import tech.bilal.embedded_keycloak.KeycloakData.{ApplicationUser, ResourceRole}
import tech.bilal.embedded_keycloak.utils.{BearerToken, Ports}
import tech.bilal.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationDouble

class DataFeederTest extends FunSuite with Matchers with BeforeAndAfterAll {
  test("test data integration") {

    val settings = Settings.default.copy(port = 9005, version = "4.6.0")
    val keycloakData = KeycloakData.fromConfig
    val keycloak = new EmbeddedKeycloak(keycloakData, settings)
    val stopHandle = Await.result(keycloak.startServerInBackground(), 2.minutes)

    implicit val bearerToken: BearerToken =
      BearerToken.fromServer(9005, "admin", "admin")

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
          resourceRoles =
            Set(ResourceRole("${client_account}", "view-profile"),
                ResourceRole("${client_account}", "manage-account"))
        ),
        ApplicationUser(
          "user2",
          "[HIDDEN]",
          realmRoles = Set("uma_authorization", "offline_access"),
          resourceRoles = Set(
            ResourceRole(clientName = "some-server", roleName = "server-user"),
            ResourceRole("${client_account}", "view-profile"),
            ResourceRole("${client_account}", "manage-account")
          )
        )
      )

      clients.find(c => {
        c.name == "some-server" &&
        c.authorizationEnabled &&
        c.resourceRoles.contains("server-admin") &&
        c.resourceRoles.contains("server-user")
      }) should not be empty

      clients.find(c => {
        c.name == "some-client" &&
        !c.authorizationEnabled
      }) should not be empty

      stopHandle.stop()
    }
  }

  override def afterAll(): Unit = {
    new Ports().stop(9005)
  }
}
