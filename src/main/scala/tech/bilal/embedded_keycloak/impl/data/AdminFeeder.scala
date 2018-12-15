package tech.bilal.embedded_keycloak.impl.data

import os.Path
import tech.bilal.embedded_keycloak.KeycloakData.AdminUser
import tech.bilal.embedded_keycloak.Settings
import tech.bilal.embedded_keycloak.impl.Bash.exec

private[embedded_keycloak] class AdminFeeder(settings: Settings)
    extends FeederBase(settings) {

  import settings._

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  def feedAdminUser(admin: AdminUser): Int =
    exec(
      s"sh ${getBinDirectory / "add-user-keycloak.sh"} " +
        s"--user ${admin.username} " +
        s"-p ${admin.password}")
}
