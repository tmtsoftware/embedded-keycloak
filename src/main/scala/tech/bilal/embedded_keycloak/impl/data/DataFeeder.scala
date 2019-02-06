package tech.bilal.embedded_keycloak.impl.data

import tech.bilal.embedded_keycloak.utils.BearerToken
import tech.bilal.embedded_keycloak.{KeycloakData, Settings}

import scala.language.implicitConversions

class DataFeeder(settings: Settings) {

  val adminFeeder = new RestAdminFeeder(settings)
  val realmFeeder = new RealmFeeder(settings)

  def feed(keycloakData: KeycloakData): Unit = {
    //feed admin
    adminFeeder.feedAdminUser(keycloakData.adminUser)

    //feed realm
    implicit val bearerToken: BearerToken =
      BearerToken.fromServer(settings.port,
                             keycloakData.adminUser.username,
                             keycloakData.adminUser.password)

    keycloakData.realms.foreach(realmFeeder.feedRealm)
  }
}
