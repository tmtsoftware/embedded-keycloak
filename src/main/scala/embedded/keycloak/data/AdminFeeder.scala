package embedded.keycloak.data

import embedded.keycloak.internal.Bash.exec
import embedded.keycloak.models.KeycloakData.AdminUser
import embedded.keycloak.models.Settings
import os.Path

class AdminFeeder(settings: Settings) extends FeederBase(settings) {

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
