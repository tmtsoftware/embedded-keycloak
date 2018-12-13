package embedded.keycloak.download

import embedded.keycloak.internal.Bash.exec
import embedded.keycloak.models.Settings
import os.Path

class CurlDownloader(settings: Settings) {

  def download(): Unit = {

    import settings._
    val url =
      s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

    exec(s"curl -O -# $url", Path(installationDirectory) / version)
  }
}
