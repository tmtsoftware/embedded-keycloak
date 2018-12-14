package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.data.DataFeeder
import embedded.keycloak.download.AkkaDownloader
import embedded.keycloak.internal.Bash._
import embedded.keycloak.models.{Data, Settings}
import os.Path

import scala.concurrent.Future

class Installer(settings: Settings, data: Data)(
    implicit actorSystem: ActorSystem) {

  implicit val ec = actorSystem.dispatcher
  import settings._

  val downloader = new AkkaDownloader(settings)
  val dataFeeder = new DataFeeder(settings, data)

  private def getInstallationDirectory = Path(installationDirectory) / version

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"

  private def clean(): Unit = {
    os.remove.all(getInstallationDirectory)
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
    if (cleanInstall) clean()

    if (!isKeycloakInstalled) {
      downloader.download()
      decompress()
      dataFeeder.feedAdminUser()
    } else {
      Future.successful(())
    }
  }
}
