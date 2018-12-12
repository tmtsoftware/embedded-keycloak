package embedded.keycloak.internal

import embedded.keycloak.models.Settings
import os.Path
import Bash._
import scala.concurrent.{ExecutionContext, Future}

class Installer(settings: Settings) {

  import settings._

  val downloader = new Downloader(settings)

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
    val wd = getInstallationDirectory
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

  def install()(implicit ec: ExecutionContext): Future[Unit] = {
    if (cleanInstall) clean()

    if (!isKeycloakInstalled) {

      downloader
        .download()
        .map(_ => {
          decompress()
          addAdmin()
        })

    } else {
      Future.successful(())
    }
  }
}
