package embedded.keycloak.data

import embedded.keycloak.internal.Bash.exec
import embedded.keycloak.models.{Data, Settings}
import os.Path

class DataFeeder(settings: Settings, data: Data) {
  import settings._

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  //todo: roles of the admin user
  def feedAdminUser() =
    exec(
      s"sh ${getBinDirectory / "add-user-keycloak.sh"} --user ${data.adminUser.username} -p ${data.adminUser.password}")

  def feedRealm() = ???
}
