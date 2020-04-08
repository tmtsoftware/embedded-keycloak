package org.tmt.embedded_keycloak.impl.data

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.tmt.embedded_keycloak.KeycloakData
import org.tmt.embedded_keycloak.KeycloakData._

class DataParserTest extends AnyFunSuite with Matchers {
  test("testParsing") {
    val parsedData: KeycloakData = KeycloakData.fromConfig

    val expectedData = KeycloakData(
      adminUser = AdminUser(),
      realms = Set(
        Realm(
          name = "example-realm",
          clients = Set(
            Client(
              name = "some-server",
              clientType = "bearer-only",
              clientRoles = Set("server-admin", "server-user")
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
