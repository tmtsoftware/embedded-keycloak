package tech.bilal.embedded_keycloak.impl.data

import requests.{RequestBlob, get, post}
import tech.bilal.embedded_keycloak.KeycloakData.AdminUser
import tech.bilal.embedded_keycloak.Settings
import tech.bilal.embedded_keycloak.impl.Bash.exec
import tech.bilal.embedded_keycloak.impl.FileIO

sealed trait AdminFeeder {
  def feedAdminUser(admin: AdminUser): Unit
}

private[embedded_keycloak] class JavaAdminFeeder(settings: Settings)
    extends FeederBase(settings)
    with AdminFeeder {

  val fileIO = new FileIO(settings)

  override def feedAdminUser(admin: AdminUser): Unit =
    exec(
      s"sh ${fileIO.addUserExecutablePath} " +
        s"--user ${admin.username} " +
        s"-p ${admin.password}")
}

private[embedded_keycloak] class RestAdminFeeder(settings: Settings)
    extends FeederBase(settings)
    with AdminFeeder {

  override def feedAdminUser(admin: AdminUser): Unit = {
    val origin = s"http://${settings.host}:${settings.port}"
    val referer = origin + "/"
    val url = referer + "auth/"
    val cookieName = "WELCOME_STATE_CHECKER"

    val getResponse = get(
      url = url,
      headers = Map(
        "Connection" -> "keep-alive",
        "Pragma" -> "no-cache",
        "Cache-Control" -> "Cache-Control",
        "Upgrade-Insecure-Requests" -> "1",
        "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36",
        "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
        "Referer" -> referer,
        "Accept-Encoding" -> "gzip, deflate, br",
        "Accept-Language" -> "en-US,en;q=0.9"
      ),
    )

    if (getResponse.statusCode != 200)
      throw new RuntimeException("could not create admin user")

    val cookies = getResponse.cookies

    if (cookies.contains(cookieName)) {
      val state = cookies(cookieName).getValue

      val postResponse = post(
        url = url,
        headers = Map(
          "Connection" -> "keep-alive",
          "Cache-Control" -> "max-age=0",
          "Origin" -> origin,
          "Upgrade-Insecure-Requests" -> "1",
          "Content-Type" -> "application/x-www-form-urlencoded",
          "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36",
          "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
          "Referer" -> url,
          "Accept-Language" -> "en-US,en;q=0.9,gl;q=0.8,de;q=0.7"
        ),
        cookies = cookies,
        data = RequestBlob.FormEncodedRequestBlob(
          Map(
            "username" -> admin.username,
            "password" -> admin.password,
            "passwordConfirmation" -> admin.password,
            "stateChecker" -> state
          )
        )
      )

      if (postResponse.statusCode != 200)
        throw new RuntimeException("could not create admin user")
    }
  }
}
