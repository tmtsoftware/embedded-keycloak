package tech.bilal.embedded_keycloak.impl

import os.Path
import tech.bilal.embedded_keycloak.impl.Bash._
import tech.bilal.embedded_keycloak.impl.data.AdminFeeder
import tech.bilal.embedded_keycloak.impl.download.{
  AkkaDownloader,
  CurlDownloader
}
import tech.bilal.embedded_keycloak.{KeycloakData, Settings}

class Installer(settings: Settings, data: KeycloakData) {

  import settings._

  val downloader = new AkkaDownloader(settings)
  val adminFeeder = new AdminFeeder(settings)

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"

  private def cleanInstallation(): Unit = {
    if (os.exists(getKeycloakRoot)) os.remove.all(getKeycloakRoot)
  }

  private def isKeycloakInstalled: Boolean = {
    val wd = getBinDirectory / "standalone.sh"
    os.exists(wd)
  }

  private def decompress(): Unit = {
    os.makeDir.all(getKeycloakRoot)
    exec(s"tar -xzf $getTarFilePath -C $getKeycloakRoot")
  }

  def install(): Unit = {

    downloader.download()

    if (cleanPreviousData || !isKeycloakInstalled) {
      cleanInstallation()
      decompress()
    }

    adminFeeder.feedAdminUser(data.adminUser)
  }
}
