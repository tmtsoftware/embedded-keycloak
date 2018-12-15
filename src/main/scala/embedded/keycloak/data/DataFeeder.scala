package embedded.keycloak.data

import embedded.keycloak.internal.Bash.exec
import embedded.keycloak.models.KeycloakData.{ApplicationUser, Client}
import embedded.keycloak.models.{KeycloakData, Settings}
import os.Path
import requests._
import ujson.{Bool, Obj, Str, Value}

import scala.collection.mutable.{LinkedHashMap => MutableMap}
import scala.io.Source
import scala.language.implicitConversions

class DataFeeder(settings: Settings, data: KeycloakData) {

  case class BearerToken(token: String) extends RequestAuth {
    override def header: Option[String] = Some(s"Bearer $token")
  }

  import settings._

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  private val jTrue = Bool(true)
  private val jFalse = Bool(false)

  //todo: roles of the admin user
  def feedAdminUser(): Int =
    exec(
      s"sh ${getBinDirectory / "add-user-keycloak.sh"} --user ${data.adminUser.username} -p ${data.adminUser.password}")

  private def getBearerToken: BearerToken = {
    val response = get(
      url =
        s"http://localhost:$port/auth/realms/master/protocol/openid-connect/token",
      headers = Map("Content-Type" -> "application/x-www-form-urlencoded"),
      data = RequestBlob.FormEncodedRequestBlob(
        Map(
          "client_id" -> "admin-cli",
          "grant_type" -> "password",
          "username" -> data.adminUser.username,
          "password" -> data.adminUser.password
        ))
    )

    if (response.statusCode != 200)
      throw new RuntimeException(s"Could not log in to keycloak")

    val tokenString =
      ujson.read(response.data.bytes).obj.get("access_token").map(_.str).get
    BearerToken(tokenString)
  }

  private def feedClient(client: Client, realmName: String)(
      implicit bearerToken: BearerToken): Unit = {

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

    val response = kPost(realmUrl(realmName) + "/clients", payload)

    if (response.statusCode != 201)
      throw new RuntimeException(
        s"Could not create client $client.\nServer response: ${response.statusCode}\n${response.data.text}")

    client.resourceRoles.foreach(r =>
      feedResourceRole(r, getId(response), realmName))
  }

  private def getId(response: Response): String = {
    val url = response.headers("location").head
    url.split("/").last
  }

  private def feedRealmRole(roleName: String, realmName: String)(
      implicit bearerToken: BearerToken): Unit = {
    val response =
      kPost(url = realmUrl(realmName) + "/roles", Map("name" -> Str(roleName)))

    if (response.statusCode != 201)
      throw new RuntimeException(
        s"Could not create role $roleName.\nServer response: ${response.statusCode}\n${response.data.text}")
  }

  private def feedResourceRole(
      roleName: String,
      clientId: String,
      realmName: String)(implicit bearerToken: BearerToken): Unit = {
    val response = kPost(realmUrl(realmName) + s"/clients/$clientId/roles",
                         Map(
                           "name" -> Str(roleName),
                         ))

    if (response.statusCode != 201)
      throw new RuntimeException(
        s"Could not create role $roleName.\nServer response: ${response.statusCode}\n${response.data.text}")
  }

  def feedUser(user: ApplicationUser, realmName: String)(
      implicit bearerToken: BearerToken): Unit = {
    val creationResponse = kPost(
      url = realmUrl(realmName) + "/users",
      Map(
        "enabled" -> jTrue,
        "attributes" -> ujson.Arr(),
        "username" -> Str(user.username),
        "emailVerified" -> Str(""),
        "firstName" -> Str(user.firstName),
        "lastName" -> Str(user.lastName)
      )
    )

    if (creationResponse.statusCode != 201)
      throw new RuntimeException(
        s"could not create user ${user.username}. status: ${creationResponse.statusCode}\n" +
          s"response: ${creationResponse.text()}")

    val userId = getId(creationResponse)

    val resetResponse = kPut(
      url = s"${realmUrl(realmName)}/users/$userId/reset-password",
      Map(
        "type" -> Str("password"),
        "value" -> Str(user.password),
        "temporary" -> jFalse
      ))

    if (resetResponse.statusCode != 200 && resetResponse.statusCode != 204)
      throw new RuntimeException(
        s"could not set password for user ${user.username}. status: ${resetResponse.statusCode}\n" +
          s"response: ${resetResponse.text()}")

    mapRealmRoles(realmName, userId, user.realmRoles)
  }

  case class RoleRepresentation(name: String,
                                id: String,
                                containerId: String,
                                composite: Boolean,
                                clientRole: Boolean)

  object RoleRepresentation {
    import upickle.default.{ReadWriter => RW, macroRW}
    implicit val rw: RW[RoleRepresentation] = macroRW
  }

  def mapRealmRoles(realmName: String, userId: String, roleNames: Set[String])(
      implicit bearerToken: BearerToken): Unit = {

    val realmRoles = {
      val rolesResponse = kGet(realmUrl(realmName) + "/roles")
      if (rolesResponse.statusCode != 200 && rolesResponse.statusCode != 204)
        throw new RuntimeException(
          s"could not get realmRoles for user id $userId. status: ${rolesResponse.statusCode}\n" +
            s"response: ${rolesResponse.text()}")
      upickle.default
        .read[Set[RoleRepresentation]](rolesResponse.text())
        .filter(r => roleNames.contains(r.name))
    }

    //204 no content
    val url = realmUrl(realmName) + s"/users/$userId/role-mappings/realm"

    val response = kPost(url, upickle.default.write(realmRoles))

    if (response.statusCode != 200 && response.statusCode != 204)
      throw new RuntimeException(
        s"could not add realmRoles for user id $userId. status: ${response.statusCode}\n" +
          s"response: ${response.text()}")
  }

  def feedRealms: Unit = {
    implicit val bearerToken: BearerToken = getBearerToken

    data.realms.foreach(r => {
      val response = kPost(realmUrl,
                           Map(
                             "enabled" -> jTrue,
                             "id" -> Str(r.name),
                             "realm" -> Str(r.name)
                           ))
      if (response.statusCode != 201)
        throw new RuntimeException(
          s"could not create realm ${r.name}. status: ${response.statusCode}\n" +
            s"response: ${response.text()}")

      r.clients.foreach(feedClient(_, r.name))
      r.realmRoles.foreach(feedRealmRole(_, r.name))
      r.users.foreach(feedUser(_, r.name))
    })
  }

  private implicit def toMutableMap(
      map: Map[String, Value]): MutableMap[String, Value] = {
    val mutableMap = MutableMap[String, Value]()
    map.foreach(mutableMap += _)
    mutableMap
  }

  private implicit def toString(map: Map[String, Value]): String =
    ujson.write(Obj(map))

  private def realmUrl = s"http://localhost:$port/auth/admin/realms"

  private def realmUrl(realmName: String): String =
    s"http://localhost:$port/auth/admin/realms/$realmName"

  private def kPost(url: String, data: String)(
      implicit bearerToken: BearerToken): Response = {
    sendRequest(requester("POST"), url, data)
  }

  private def kPut(url: String, data: String)(
      implicit bearerToken: BearerToken): Response = {
    sendRequest(requester("PUT"), url, data)
  }

  private def kGet(url: String)(implicit bearerToken: BearerToken): Response = {
    sendRequest(requester("GET"), url)
  }

  private def requester(method: String) = {
    method match {
      case "POST" => post
      case "GET"  => get
      case "PUT"  => put
    }
  }

  private def sendRequest(
      requester: Requester,
      url: String,
      data: String = null)(implicit bearerToken: BearerToken): Response = {
    requester(
      url = url,
      auth = bearerToken,
      data =
        if (data == null) RequestBlob.EmptyRequestBlob
        else RequestBlob.StringRequestBlob(data),
      headers = Map(
        "Content-Type" -> "application/json"
      )
    )
  }
}
