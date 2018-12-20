package tech.bilal.embedded_keycloak.impl.data

import tech.bilal.embedded_keycloak.utils.BearerToken
import tech.bilal.embedded_keycloak.{KeycloakData, Settings}

import scala.language.implicitConversions

class DataFeeder(settings: Settings) {

  val realmFeeder = new RealmFeeder(settings)

  def feed(keycloakData: KeycloakData): Unit = {
    implicit val bearerToken: BearerToken =
      BearerToken.getBearerToken(settings.port,
                                 keycloakData.adminUser.username,
                                 keycloakData.adminUser.password)

    keycloakData.realms.foreach(realmFeeder.feedRealm)
  }
}
