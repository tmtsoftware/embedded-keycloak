package tech.bilal.embedded_keycloak.impl

import os.Path
import tech.bilal.embedded_keycloak.Settings

import scala.language.implicitConversions

class FileIO(settings: Settings) {
  import settings._

  private[this] implicit def toPath(path: String): Path = Path(path)

  //PATHS

  private val versionDirectory: Path = keycloakDirectory / version

  def downloadDirectory: Path = versionDirectory / "downloads"

  def binariesDirectory: Path = versionDirectory / "binaries"

  def tarFilePath: Path = downloadDirectory / s"keycloak-$version.Final.tar.gz"

  def addUserExecutablePath: Path =
    binariesDirectory / s"keycloak-$version.Final" / "bin" / "add-user-keycloak.sh"

  def keycloakExecutablePath: Path =
    binariesDirectory / s"keycloak-$version.Final" / "bin" / "standalone.sh"

  // CHECKS

  def isKeycloakInstalled: Boolean = os.exists(keycloakExecutablePath)

  def isKeycloakDownloaded: Boolean = os.exists(tarFilePath)

  //OPERATIONS

  def deleteVersion(): Unit = os.remove.all(versionDirectory)

  def deleteBinaries(): Unit = os.remove.all(binariesDirectory)
}
