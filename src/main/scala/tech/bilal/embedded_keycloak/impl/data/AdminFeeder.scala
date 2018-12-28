package tech.bilal.embedded_keycloak.impl.data

import tech.bilal.embedded_keycloak.KeycloakData.AdminUser
import tech.bilal.embedded_keycloak.Settings
import tech.bilal.embedded_keycloak.impl.Bash.exec
import tech.bilal.embedded_keycloak.impl.FileIO

private[embedded_keycloak] class AdminFeeder(settings: Settings)
    extends FeederBase(settings) {

  val fileIO = new FileIO(settings)

  def feedAdminUser(admin: AdminUser): Int =
    exec(
      s"sh ${fileIO.addUserExecutablePath} " +
        s"--user ${admin.username} " +
        s"-p ${admin.password}")
}
