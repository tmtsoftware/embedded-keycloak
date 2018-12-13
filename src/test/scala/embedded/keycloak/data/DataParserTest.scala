package embedded.keycloak.data

import org.scalatest.{FunSuite, Matchers}

class DataParserTest extends FunSuite with Matchers {
  test("testParsing") {
    val parsedData = DataParser.parse

    val expectedData = Data(
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
