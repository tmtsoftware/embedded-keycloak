package org.tmt.embedded_keycloak.impl

import org.tmt.embedded_keycloak.Settings
import os.Path

import scala.language.implicitConversions

class FileIO(settings: Settings) {
  import settings._
  private[this] implicit def toPath(path: String): Path = Path(path)

  //PATHS
  private val versionDirectory: Path = keycloakDirectory / version

  def downloadDirectory: Path      = versionDirectory / "downloads"
  def binariesDirectory: Path      = versionDirectory / "binaries"
  def incompleteTarFilePath: Path  = downloadDirectory / s"keycloak-$version.tar.gz.incomplete"
  def tarFilePath: Path            = downloadDirectory / s"keycloak-$version.tar.gz"
  def addUserExecutablePath: Path  = binariesDirectory / s"keycloak-$version" / "bin" / "add-user-keycloak.sh"
  def keycloakExecutablePath: Path = binariesDirectory / s"keycloak-$version" / "bin" / "standalone.sh"

  // CHECKS
  def isKeycloakInstalled: Boolean  = os.exists(keycloakExecutablePath)
  def isKeycloakDownloaded: Boolean = os.exists(tarFilePath)

  //OPERATIONS
  def deleteVersion(): Unit      = os.remove.all(versionDirectory)
  def deleteBinaries(): Unit     = os.remove.all(binariesDirectory)
  def moveIncompleteFile(): Unit =
    os.move(
      from = incompleteTarFilePath,
      to = tarFilePath,
      replaceExisting = true,
      atomicMove = true,
      createFolders = true
    )
}
