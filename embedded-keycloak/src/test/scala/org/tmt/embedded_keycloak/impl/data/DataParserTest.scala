package org.tmt.embedded_keycloak.impl.data

import org.scalatest.{FunSuite, Matchers}
import org.tmt.embedded_keycloak.KeycloakData
import org.tmt.embedded_keycloak.KeycloakData._

class DataParserTest extends FunSuite with Matchers {
  test("testParsing") {
    val parsedData: KeycloakData = KeycloakData.fromConfig

    val expectedData = KeycloakData(
      adminUser = AdminUser("admin", "admin"),
      realms = Set(
        Realm(
          name = "example-realm",
          clients = Set(
            Client(
              name = "some-server",
              clientType = "confidential",
              clientRoles = Set("server-admin", "server-user"),
              authorizationEnabled = true
            ),
            Client(name = "some-client")
          ),
          users = Set(
            ApplicationUser(
              username = "user1",
              firstName = "john",
              password = "abcd",
              realmRoles = Set("super-admin")
            ),
            ApplicationUser(
              username = "user2",
              password = "abcd",
              clientRoles = Set(ClientRole("some-server", "server-user"))
            )
          ),
          realmRoles = Set("super-admin")
        )
      )
    )

    parsedData shouldBe expectedData
  }
}
