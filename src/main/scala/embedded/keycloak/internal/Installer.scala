package embedded.keycloak.internal

import embedded.keycloak.models.Settings
import os.Path

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

    val commandResult = os
      .proc("tar", "-xzf", getTarFilePath, "-C", getKeycloakRoot)
      .call(cwd = getInstallationDirectory)
    if (commandResult.exitCode != 0)
      throw new RuntimeException(
        s"could not decompress keycloak tar file. exit code ${commandResult.exitCode}")
  }

  private def addAdmin(): Unit = {
    val result = os
      .proc("sh",
            getBinDirectory / "add-user-keycloak.sh",
            "--user",
            username,
            "-p",
            password)
      .call()
    if (result.exitCode != 0)
      throw new RuntimeException(
        "could not add admin user. " +
          s"exit code ${result.exitCode}")
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
