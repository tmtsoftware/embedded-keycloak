package embedded.keycloak.internal

import akka.actor.ActorSystem
import embedded.keycloak.download.AkkaDownloader
import embedded.keycloak.internal.Bash._
import embedded.keycloak.models.Settings
import os.Path

import scala.concurrent.Future

class Installer(settings: Settings)(implicit actorSystem: ActorSystem) {

  implicit val ec = actorSystem.dispatcher
  import settings._

  val downloader = new AkkaDownloader(settings)

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

  private def addAdmin(): Unit = {
    exec(
      s"sh ${getBinDirectory / "add-user-keycloak.sh"} --user $username -p $password")
  }

  def install(): Unit = {
    if (cleanInstall) clean()

    if (!isKeycloakInstalled) {
      downloader.download()
      decompress()
      addAdmin()
    } else {
      Future.successful(())
    }
  }
}
