package org.tmt.embedded_keycloak.impl.data

import org.tmt.embedded_keycloak.KeycloakData.AdminUser
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.Bash.exec
import org.tmt.embedded_keycloak.impl.FileIO
import requests.{get, post, RequestBlob}

sealed trait AdminFeeder {
  def feedAdminUser(admin: AdminUser): Unit
}

private[embedded_keycloak] class JavaAdminFeeder(settings: Settings, fileIO: FileIO)
    extends FeederBase(settings)
    with AdminFeeder {

  override def feedAdminUser(admin: AdminUser): Unit =
    exec(
      settings.stdOutLogger,
      "sh",
      fileIO.addUserExecutablePath.toString,
      s"--user ${admin.username}",
      s"-p ${admin.password}"
    )
}

private[embedded_keycloak] class RestAdminFeeder(settings: Settings) extends FeederBase(settings) with AdminFeeder {

  override def feedAdminUser(admin: AdminUser): Unit = {
    val origin     = s"http://localhost:${settings.port}"
    val referer    = origin + "/"
    val url        = referer + "auth/"
    val cookieName = "WELCOME_STATE_CHECKER"

    val getResponse = get(url = url)

    if (getResponse.statusCode != 200) throw new RuntimeException("could not create admin user")

    val cookies = getResponse.cookies

    if (cookies.contains(cookieName)) {
      val state = cookies(cookieName).getValue

      val postResponse = post(
        url = url,
        cookies = cookies,
        data = RequestBlob.FormEncodedRequestBlob(
          Map(
            "username"             -> admin.username,
            "password"             -> admin.password,
            "passwordConfirmation" -> admin.password,
            "stateChecker"         -> state
          )
        )
      )

      if (postResponse.statusCode != 200) throw new RuntimeException("could not create admin user")
    }
  }
}
