package tech.bilal.embedded_keycloak

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import tech.bilal.embedded_keycloak.KeycloakData.{AdminUser, Realm}

case class KeycloakData(adminUser: AdminUser = AdminUser.default,
                        realms: Set[Realm] = Set.empty)

object KeycloakData {

  lazy val empty: KeycloakData = KeycloakData()
  lazy val fromConfig: KeycloakData = ConfigFactory
    .load()
    .getConfig("embedded-keycloak")
    .as[KeycloakData]

  case class Realm(name: String,
                   realmRoles: Set[String] = Set.empty,
                   clients: Set[Client] = Set.empty,
                   users: Set[ApplicationUser] = Set.empty)

  case class ResourceRole(clientName: String, roleName: String)

  case class Client(name: String,
                    clientType: String = "public",
                    authorizationEnabled: Boolean = false,
                    resourceRoles: Set[String] = Set.empty)

  case class AdminUser(username: String = "admin", password: String = "admin")

  object AdminUser {
    val default: AdminUser = AdminUser()
  }

  case class ApplicationUser(username: String,
                             password: String,
                             firstName: String = "",
                             lastName: String = "",
                             realmRoles: Set[String] = Set.empty,
                             resourceRoles: Set[ResourceRole] = Set.empty)
}
