package org.tmt.embedded_keycloak.utils

import requests.{RequestAuth, post}

case class BearerToken(token: String) extends RequestAuth {
  override def header: Option[String] = Some(s"Bearer $token")
}

object BearerToken {
  def fromServer(
      port: Int,
      username: String,
      password: String,
      realm: String = "master",
      client: String = "admin-cli",
      host: String = "localhost"
  ): BearerToken = {
    val response = post(
      url = s"http://$host:$port/realms/$realm/protocol/openid-connect/token",
      headers = Map("Content-Type" -> "application/x-www-form-urlencoded"),
      data = Map(
        "client_id"  -> client,
        "grant_type" -> "password",
        "username"   -> username,
        "password"   -> password
      )
    )

    if (response.statusCode != 200) {
      val error = Seq("Could not log in to keycloak", response.text())
        .filter(_ != "")
        .mkString("\n")
      throw new RuntimeException(error)
    }

    val tokenString = ujson.read(response.bytes).obj.get("access_token").map(_.str).get
    BearerToken(tokenString)
  }
}
