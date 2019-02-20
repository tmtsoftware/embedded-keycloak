package org.tmt.embedded_keycloak.impl

import org.tmt.embedded_keycloak.impl.Bash._
import org.tmt.embedded_keycloak.impl.download.AkkaDownloader
import org.tmt.embedded_keycloak.{KeycloakData, Settings}

class Installer(settings: Settings, data: KeycloakData, fileIO: FileIO) {

  import settings._

  val downloader = new AkkaDownloader(settings, fileIO)

  private def extractArchive(): Unit = {
    os.makeDir.all(fileIO.binariesDirectory)
    exec("tar", "-xzf", fileIO.tarFilePath.toString, "-C", s"${fileIO.binariesDirectory}")
  }

  def install(): Unit = {
    downloader.download()

    if (cleanPreviousData || !fileIO.isKeycloakInstalled) {
      fileIO.deleteBinaries()
      extractArchive()
    }
  }
}
