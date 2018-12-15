package embedded.keycloak.data

import embedded.keycloak.models.KeycloakData.AdminUser
import requests.{RequestAuth, RequestBlob, get}

case class BearerToken(token: String) extends RequestAuth {
  override def header: Option[String] = Some(s"Bearer $token")
}

object BearerToken {
  def getBearerToken(port: Int, adminUser: AdminUser): BearerToken = {
    val response = get(
      url =
        s"http://localhost:$port/auth/realms/master/protocol/openid-connect/token",
      headers = Map("Content-Type" -> "application/x-www-form-urlencoded"),
      data = RequestBlob.FormEncodedRequestBlob(
        Map(
          "client_id" -> "admin-cli",
          "grant_type" -> "password",
          "username" -> adminUser.username,
          "password" -> adminUser.password
        ))
    )

    if (response.statusCode != 200)
      throw new RuntimeException(s"Could not log in to keycloak")

    val tokenString =
      ujson.read(response.data.bytes).obj.get("access_token").map(_.str).get
    BearerToken(tokenString)
  }
}
