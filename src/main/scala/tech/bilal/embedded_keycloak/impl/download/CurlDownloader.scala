package tech.bilal.embedded_keycloak.impl.download

import os.{Path, proc}
import tech.bilal.embedded_keycloak.Settings

private[embedded_keycloak] class CurlDownloader(settings: Settings)
    extends Downloader {

  import settings._

  private def getInstallationDirectory = Path(installationDirectory) / version

  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"

  private def isKeycloakDownloaded: Boolean = {
    os.exists(getTarFilePath)
  }

  private def cleanEverything(): Unit = {
    os.remove.all(getInstallationDirectory)
  }

  protected val url =
    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

  def download(): Unit = {

    if (alwaysDownload || !isKeycloakDownloaded) {
      cleanEverything()
      os.makeDir.all(Path(installationDirectory) / version)

      println("downloading keycloak...")

      var line: String = ""

      val reWriteLine: (Array[Byte], Int) => Unit = (buf, len) => {
        val str = buf.slice(0, len).map(_.toChar).mkString
        if (str.contains("\r")) {
          line = str.split("\r").lastOption.getOrElse("")
        } else {
          line += str
        }
        print("\r" + line)
      }

      val exitCode = proc("curl", "-#", "-O", url)
        .stream(
          onOut = reWriteLine,
          onErr = reWriteLine,
          cwd = Path(installationDirectory) / version
        )

      if (exitCode != 0)
        throw new RuntimeException("could not download keycloak")
      else
        exitCode
    }
  }
}
