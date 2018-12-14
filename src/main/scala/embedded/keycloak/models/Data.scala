package embedded.keycloak.models

case class Data(adminUser: AdminUser = AdminUser.default,
                realms: Set[Realm] = Set.empty)

case class Realm(name: String,
                 realmRoles: Set[String] = Set.empty,
                 clients: Set[Client] = Set.empty,
                 users: Set[ApplicationUser] = Set.empty)

case class Client(name: String,
                  clientType: String,
                  resourceRoles: Set[String] = Set.empty)

trait User {
  val username: String
  val password: String
  val realmRoles: Set[String]
  val resourceRoles: Set[String]
}

case class AdminUser(username: String,
                     password: String,
                     realmRoles: Set[String] = Set.empty,
                     resourceRoles: Set[String] = Set.empty)
    extends User

object AdminUser {
  val default: AdminUser = AdminUser(username = "admin", password = "admin")
}

case class ApplicationUser(username: String,
                           password: String,
                           realmRoles: Set[String] = Set.empty,
                           resourceRoles: Set[String] = Set.empty)
    extends User
