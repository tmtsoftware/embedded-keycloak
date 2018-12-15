package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.data.AdminFeeder
import embedded.keycloak.download.CurlDownloader
import embedded.keycloak.internal.Bash._
import embedded.keycloak.models.{KeycloakData, Settings}
import os.Path

class Installer(settings: Settings, data: KeycloakData)(
    implicit actorSystem: ActorSystem) {

  implicit val ec = actorSystem.dispatcher
  import settings._

  val downloader = new CurlDownloader(settings)
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
