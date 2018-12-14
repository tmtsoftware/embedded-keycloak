package embedded.keycloak.data

import embedded.keycloak.models.KeycloakData._
import embedded.keycloak.models._
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class DataParserTest extends FunSuite with Matchers {
  test("testParsing") {
    val parsedData: KeycloakData = KeycloakData.fromConfig

    val expectedData = KeycloakData(
      adminUser = AdminUser("admin", "admin"),
      realms = Set(
        Realm(
          name = "example-realm",
          clients = Set(Client(
                          name = "some-server",
                          clientType = "bearer-only",
                          resourceRoles = Set("server-admin", "server-user")
                        ),
                        Client(name = "some-client", clientType = "public")),
          users = Set(
            ApplicationUser(
              username = "user1",
              password = "abcd",
              realmRoles = Set("super-admin")
            ),
            ApplicationUser(
              username = "user2",
              password = "abcd",
              resourceRoles = Set("server-user")
            )
          ),
          realmRoles = Set("super-admin")
        ))
    )

    parsedData shouldBe expectedData
  }
}
