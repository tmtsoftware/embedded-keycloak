package embedded.keycloak.data

import embedded.keycloak.models.KeycloakData.{Realm, ResourceRole}
import embedded.keycloak.models.Settings

class RoleMapper(clientIds: Map[String, String],
                 realm: Realm,
                 settings: Settings)
    extends FeederBase(settings) {
  def mapRealmRoles(userId: String, roleNames: Set[String])(
      implicit bearerToken: BearerToken): Unit = {

    val realmRoles = {
      val rolesResponse = kGet(realmUrl(realm.name) + "/roles")
      upickle.default
        .read[Set[RoleRepresentation]](rolesResponse.text())
        .filter(r => roleNames.contains(r.name))
    }

    val url = realmUrl(realm.name) + s"/users/$userId/role-mappings/realm"

    kPost(url, upickle.default.write(realmRoles))
  }

  def mapResourceRoles(userId: String, resourceRoles: Set[ResourceRole])(
      implicit bearerToken: BearerToken): Unit = {

    resourceRoles
      .groupBy(x => x.clientName)
      .map {
        case (k, v) => (k, v.map(_.roleName))
      }
      .foreach {
        case (clientName, groupedRoles) =>
          val clientId = clientIds(clientName)

          val clientRolesResponse = kGet(
            realmUrl(realm.name) + s"/users/$userId/role-mappings/clients/$clientId/available")
          val clientRoles =
            upickle.default
              .read[Set[RoleRepresentation]](clientRolesResponse.text())
              .filter(r => groupedRoles.contains(r.name))

          kPost(
            realmUrl(realm.name) + s"/users/$userId/role-mappings/clients/$clientId",
            upickle.default.write(clientRoles))
      }

    //--get roles first

    //POST users/$USERID/role-mappings/clients/$CLIENTID : 204
    //DATA: ARRAY
    //clientRole: true
    //composite: false
    //containerId: "8410ba9c-cf02-4679-8eda-b148b4bfdbcb" --> CLIENT ID
    //id: "4b1ae72e-0cd5-4349-8a77-952f5f765c69" --> ROLE ID
    //name: "server-user"
  }
}
