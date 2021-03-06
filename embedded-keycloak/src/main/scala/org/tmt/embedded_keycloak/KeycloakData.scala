package org.tmt.embedded_keycloak

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.tmt.embedded_keycloak.KeycloakData.{AdminUser, Realm}
import org.tmt.embedded_keycloak.impl.data.DataFetcher
import org.tmt.embedded_keycloak.utils.BearerToken

case class KeycloakData(adminUser: AdminUser = AdminUser.default, realms: Set[Realm] = Set.empty)

object KeycloakData {

  lazy val empty: KeycloakData      = KeycloakData()
  lazy val fromConfig: KeycloakData = ConfigFactory
    .load()
    .getConfig("embedded-keycloak")
    .as[KeycloakData]

  def fromServer(settings: Settings, adminUsername: String, adminPassword: String): KeycloakData = {
    implicit val token: BearerToken =
      BearerToken.fromServer(settings.port, adminUsername, adminPassword)
    KeycloakData(AdminUser(adminUsername, adminPassword), new DataFetcher(settings).getRealms)
  }

  case class Realm(
      name: String,
      realmRoles: Set[String] = Set.empty,
      clients: Set[Client] = Set.empty,
      users: Set[ApplicationUser] = Set.empty
  )

  case class ClientRole(clientName: String, roleName: String)

  case class Client(
      name: String,
      clientType: String = "public",
      authorizationEnabled: Boolean = false,
      implicitFlowEnabled: Boolean = false,
      passwordGrantEnabled: Boolean = true,
      clientRoles: Set[String] = Set.empty
  )

  case class AdminUser(username: String = "admin", password: String = "admin")

  object AdminUser {
    val default: AdminUser = AdminUser()
  }

  case class ApplicationUser(
      username: String,
      password: String,
      firstName: String = "",
      lastName: String = "",
      realmRoles: Set[String] = Set.empty,
      clientRoles: Set[ClientRole] = Set.empty
  )
}
