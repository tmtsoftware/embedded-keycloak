package org.tmt.embedded_keycloak.impl.data

import org.tmt.embedded_keycloak.KeycloakData.{ApplicationUser, Client, ClientRole, Realm}
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.utils.BearerToken
import ujson.Value

import scala.collection.mutable

private[embedded_keycloak] class DataFetcher(settings: Settings) extends FeederBase(settings) {

  def getRealms(implicit bearerToken: BearerToken): Set[Realm] = {
    val response = kGet(realmUrl)
    val json     = ujson.read(response.bytes).arr
    json
      .map(_.obj)
      .map { obj =>
        val realmName = obj.getStr("realm")
        val clients   = getClients(realmName)
        Realm(
          name = realmName,
          clients = clients.values.toSet,
          users = getUsers(realmName, clients),
          realmRoles = getRealmRoles(realmName)
        )
      }
      .toSet
  }

  private def getRealmRoles(realm: String)(implicit bearerToken: BearerToken): Set[String] = {
    val realmRolesResponse = kGet(realmUrl(realm) + "/roles")
    ujson
      .read(realmRolesResponse.bytes)
      .arr
      .map(r => r.obj.get("name"))
      .toSet
      .flatten
      .map(r => r.str)
  }

  private def getClients(realm: String)(implicit bearerToken: BearerToken): Map[String, Client] = {
    val clientsResponse = kGet(realmUrl(realm) + "/clients")

    val json = ujson
      .read(clientsResponse.bytes)
      .arr

    def getClientRolesForClient(clientId: String) = {
      val response = kGet(realmUrl(realm) + s"/clients/$clientId/roles")
      ujson
        .read(response.bytes)
        .arr
        .map(_.obj)
        .map(_.getStr("name"))
        .toSet
    }

    json
      .map(_.obj)
      .map(obj => {
        obj.getStr("id") ->
        Client(
          name = obj.getStr("name"),
          clientType = obj.getClientType,
          authorizationEnabled = obj.getBool("authorizationServicesEnabled"),
          clientRoles = getClientRolesForClient(obj.getStr("id"))
        )
      })
      .toMap
  }

  private def getUsers(realm: String, clientIds: Map[String, Client])(implicit bearerToken: BearerToken): Set[ApplicationUser] = {
    val response = kGet(realmUrl(realm) + "/users")
    val arr      = ujson.read(response.bytes).arr

    def getRealmRoleMappings(userId: String): Set[String] = {
      val response = kGet(realmUrl(realm) + s"/users/$userId/role-mappings/realm")
      ujson
        .read(response.bytes)
        .arr
        .map(_.obj)
        .map(_.getStr("name"))
        .toSet
    }

    def getClientsRoleMappings(userId: String): Set[ClientRole] = {
      clientIds.keys
        .map(clientId =>
          clientId ->
            ujson
              .read(kGet(realmUrl(realm) + s"/users/$userId/role-mappings/clients/$clientId").bytes)
              .arr
              .map(_.obj)
              .map(_.getStr("name"))
        )
        .toMap
        .flatMap { case (k, v) => v.map(roleName => ClientRole(clientIds(k).name, roleName)) }
        .toSet
    }

    arr
      .map(_.obj)
      .map(obj => {
        ApplicationUser(
          username = obj.getStr("username"),
          "[HIDDEN]",
          firstName = obj.getStr("firstName"),
          lastName = obj.getStr("lastName"),
          realmRoles = getRealmRoleMappings(obj.getStr("id")),
          clientRoles = getClientsRoleMappings(obj.getStr("id"))
        )
      })
      .toSet
  }

  private[this] implicit class RichLinkedHashMap(map: mutable.LinkedHashMap[String, Value]) {
    def getStr(key: String): String = map.get(key).map(_.str).getOrElse("")

    def getBool(key: String): Boolean = map.get(key).exists(_.bool)

    def getClientType: String = {
      (map.getBool("publicClient"), map.getBool("bearerOnly")) match {
        case (true, false)  => "public"
        case (false, true)  => "bearerOnly"
        case (false, false) => "confidential"
        case _              => throw new RuntimeException("invalid client type")
      }
    }
  }
}
