package embedded.keycloak.data

import embedded.keycloak.models.KeycloakData.Realm
import embedded.keycloak.models.Settings
import ujson.Str

class RealmFeeder(settings: Settings) extends FeederBase(settings) {

  def feedRealm(realm: Realm)(implicit bearerToken: BearerToken): Unit = {

    val clientFeeder = new ClientFeeder(realm, settings)

    kPost(realmUrl,
          Map(
            "enabled" -> jTrue,
            "id" -> Str(realm.name),
            "realm" -> Str(realm.name)
          ))

    val clientIds = realm.clients.map(c => clientFeeder.feedClient(c)).toMap
    realm.realmRoles.foreach(feedRealmRole(_, realm.name))
    val userFeeder = new UserFeeder(clientIds, realm, settings)
    realm.users.foreach(userFeeder.feedUser)
  }

  private def feedRealmRole(roleName: String, realmName: String)(
      implicit bearerToken: BearerToken): Unit =
    kPost(url = realmUrl(realmName) + "/roles", Map("name" -> Str(roleName)))
}
