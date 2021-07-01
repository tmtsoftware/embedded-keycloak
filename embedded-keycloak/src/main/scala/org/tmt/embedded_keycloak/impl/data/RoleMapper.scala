package org.tmt.embedded_keycloak.impl.data

import org.tmt.embedded_keycloak.KeycloakData.{ClientRole, Realm}
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.utils.BearerToken

private[embedded_keycloak] class RoleMapper(
    clientIds: Map[String, String],
    realm: Realm,
    settings: Settings
) extends FeederBase(settings) {

  private def roleRepresentations(readable: ujson.Readable, predicate: RoleRepresentation => Boolean) =
    upickle.default
      .read[Set[RoleRepresentation]](readable)
      .filter(predicate)

  def mapRealmRoles(userId: String, roleNames: Set[String])(implicit bearerToken: BearerToken): Unit = {
    val realmRoles = {
      val rolesResponse = kGet(realmUrl(realm.name) + "/roles")
      roleRepresentations(rolesResponse.text(), r => roleNames.contains(r.name))
    }

    val url = realmUrl(realm.name) + s"/users/$userId/role-mappings/realm"

    kPost(url, upickle.default.write(realmRoles))
  }

  def mapClientRoles(userId: String, clientRoles: Set[ClientRole])(implicit bearerToken: BearerToken): Unit = {

    clientRoles
      .groupBy(x => x.clientName)
      .map { case (k, v) => (k, v.map(_.roleName)) }
      .foreach { case (clientName, groupedRoles) =>
        val clientId = clientIds(clientName)

        val clientRolesResponse = kGet(realmUrl(realm.name) + s"/users/$userId/role-mappings/clients/$clientId/available")
        val clientRoles         = roleRepresentations(clientRolesResponse.text(), r => groupedRoles.contains(r.name))

        kPost(realmUrl(realm.name) + s"/users/$userId/role-mappings/clients/$clientId", upickle.default.write(clientRoles))
      }
  }
}
