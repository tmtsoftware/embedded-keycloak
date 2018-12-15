package tech.bilal.embedded_keycloak.impl.download

import os.Path
import tech.bilal.embedded_keycloak.Settings
import tech.bilal.embedded_keycloak.impl.Bash.exec

private[embedded_keycloak] class CurlDownloader(settings: Settings) {

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

  def download(): Unit = {

    val url =
      s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

    if (alwaysDownload || !isKeycloakDownloaded) {
      cleanEverything()
      os.makeDir.all(Path(installationDirectory) / version)
      exec(s"curl -# -O $url", Path(installationDirectory) / version)
    }
  }
}
