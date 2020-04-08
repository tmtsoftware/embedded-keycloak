package org.tmt.embedded_keycloak.impl

import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.Bash._
import org.tmt.embedded_keycloak.impl.download.AkkaDownloader

class Installer(settings: Settings, fileIO: FileIO, downloader: AkkaDownloader) {
  import settings._

  private def extractArchive(): Unit = {
    os.makeDir.all(fileIO.binariesDirectory)
    exec(settings.stdOutLogger, "tar", "-xzf", fileIO.tarFilePath.toString, "-C", fileIO.binariesDirectory.toString)
  }

  def install(): Unit = {
    downloader.download()

    if (cleanPreviousData || !fileIO.isKeycloakInstalled) {
      fileIO.deleteBinaries()
      extractArchive()
    }
  }
}
