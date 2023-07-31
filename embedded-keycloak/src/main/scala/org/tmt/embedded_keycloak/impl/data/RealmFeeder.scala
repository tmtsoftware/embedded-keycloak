package org.tmt.embedded_keycloak.impl.data

import org.tmt.embedded_keycloak.KeycloakData.Realm
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.utils.BearerToken
import ujson.Str
import scala.language.implicitConversions

private[embedded_keycloak] class RealmFeeder(settings: Settings) extends FeederBase(settings) {

  def feedRealm(realm: Realm)(implicit bearerToken: BearerToken): Unit = {

    kPost(
      realmUrl,
      Map(
        "enabled" -> jTrue,
        "id"      -> Str(realm.name),
        "realm"   -> Str(realm.name)
      )
    )

    val clientFeeder = new ClientFeeder(realm, settings)
    val clientIds    = realm.clients.map(c => clientFeeder.feedClient(c)).toMap
    realm.realmRoles.foreach(feedRealmRole(_, realm.name))
    val userFeeder   = new UserFeeder(clientIds, realm, settings)
    realm.users.foreach(userFeeder.feedUser)
  }

  private def feedRealmRole(roleName: String, realmName: String)(implicit bearerToken: BearerToken): Unit =
    kPost(url = realmUrl(realmName) + "/roles", Map("name" -> Str(roleName)))
}
