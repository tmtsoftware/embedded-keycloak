package tech.bilal.embedded_keycloak.impl.data

import tech.bilal.embedded_keycloak.KeycloakData.{Client, Realm}
import tech.bilal.embedded_keycloak.Settings
import ujson.Str

import scala.io.Source

private[embedded_keycloak] class ClientFeeder(realm: Realm, settings: Settings)
    extends FeederBase(settings) {

  def feedClient(client: Client)(
      implicit bearerToken: BearerToken): (String, String) = {

    val defaultRequestString =
      Source.fromResource("create-client-request.json").mkString

    val j = ujson.read(defaultRequestString)
    j.update("name", Str(client.name))
    j.update("clientId", Str(client.name))
    j.update("authorizationServicesEnabled",
             ujson.Bool(client.authorizationEnabled))

    client.clientType match {
      case "bearer-only" =>
        j.update("bearerOnly", jTrue)
        j.update("publicClient", jFalse)
      case "public" =>
        j.update("bearerOnly", jFalse)
        j.update("publicClient", jTrue)
      case "confidential" =>
        j.update("bearerOnly", jFalse)
        j.update("publicClient", jFalse)
      case other =>
        throw new RuntimeException(s"$other client type is invalid")
    }

    val payload = ujson.write(j, indent = 4)

    val response = kPost(realmUrl(realm.name) + "/clients", payload)

    val clientId = getId(response)

    client.resourceRoles.foreach(r => feedResourceRole(r, clientId))
    (client.name, clientId)
  }

  private def feedResourceRole(roleName: String, clientId: String)(
      implicit bearerToken: BearerToken): Unit =
    kPost(realmUrl(realm.name) + s"/clients/$clientId/roles",
          Map(
            "name" -> Str(roleName),
          ))
}
