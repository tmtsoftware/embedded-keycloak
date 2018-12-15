package embedded.keycloak.data

import embedded.keycloak.models.KeycloakData.{
  ApplicationUser,
  Realm,
  ResourceRole
}
import embedded.keycloak.models.Settings
import ujson.Str

class UserFeeder(clientIds: Map[String, String],
                 realm: Realm,
                 settings: Settings)
    extends FeederBase(settings) {

  val roleMapper = new RoleMapper(clientIds, realm, settings)

  def feedUser(user: ApplicationUser)(
      implicit bearerToken: BearerToken): Unit = {
    val creationResponse = kPost(
      url = realmUrl(realm.name) + "/users",
      Map(
        "enabled" -> jTrue,
        "attributes" -> ujson.Arr(),
        "username" -> Str(user.username),
        "emailVerified" -> Str(""),
        "firstName" -> Str(user.firstName),
        "lastName" -> Str(user.lastName)
      )
    )

    val userId = getId(creationResponse)

    kPut(url = s"${realmUrl(realm.name)}/users/$userId/reset-password",
         Map(
           "type" -> Str("password"),
           "value" -> Str(user.password),
           "temporary" -> jFalse
         ))

    roleMapper.mapRealmRoles(userId, user.realmRoles)

    roleMapper.mapResourceRoles(userId, user.resourceRoles)
  }
}
