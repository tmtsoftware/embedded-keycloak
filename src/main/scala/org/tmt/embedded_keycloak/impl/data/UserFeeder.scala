package org.tmt.embedded_keycloak.impl.data

import org.tmt.embedded_keycloak.KeycloakData.{ApplicationUser, Realm}
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.utils.BearerToken
import ujson.Str

private[embedded_keycloak] class UserFeeder(clientIds: Map[String, String], realm: Realm, settings: Settings)
    extends FeederBase(settings) {

  val roleMapper = new RoleMapper(clientIds, realm, settings)

  def feedUser(user: ApplicationUser)(implicit bearerToken: BearerToken): Unit = {
    val creationResponse = kPost(
      url = realmUrl(realm.name) + "/users",
      Map(
        "enabled"       -> jTrue,
        "attributes"    -> ujson.Arr(),
        "username"      -> Str(user.username),
        "emailVerified" -> Str(""),
        "firstName"     -> Str(user.firstName),
        "lastName"      -> Str(user.lastName)
      )
    )

    val userId = getId(creationResponse)

    kPut(
      url = s"${realmUrl(realm.name)}/users/$userId/reset-password",
      Map(
        "type"      -> Str("password"),
        "value"     -> Str(user.password),
        "temporary" -> jFalse
      )
    )

    roleMapper.mapRealmRoles(userId, user.realmRoles)

    roleMapper.mapClientRoles(userId, user.clientRoles)
  }
}
