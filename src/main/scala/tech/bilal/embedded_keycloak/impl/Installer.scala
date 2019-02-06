package tech.bilal.embedded_keycloak.impl

import tech.bilal.embedded_keycloak.impl.Bash._
import tech.bilal.embedded_keycloak.impl.download.AkkaDownloader
import tech.bilal.embedded_keycloak.{KeycloakData, Settings}

class Installer(settings: Settings, data: KeycloakData) {

  import settings._

  val fileIO = new FileIO(settings)

  val downloader = new AkkaDownloader(settings)

  private def extractArchive(): Unit = {
    os.makeDir.all(fileIO.binariesDirectory)
    exec(s"tar -xzf ${fileIO.tarFilePath} -C ${fileIO.binariesDirectory}")
  }

  def install(): Unit = {
    downloader.download()

    if (cleanPreviousData || !fileIO.isKeycloakInstalled) {
      fileIO.deleteBinaries()
      extractArchive()
    }
  }
}
