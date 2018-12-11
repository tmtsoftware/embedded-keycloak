package embedded.keycloak

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

import org.backuity.clist._
import os.{Path, Source}

import scala.collection.mutable.ArrayBuffer

class Keycloak extends Command(description = "starts keycloak server") {

  var port = opt[Int](default = 8081, description = "port number to use for keycloak http server")

  var host = opt[String](default = "0.0.0.0", description = "address to bind")

  var username = opt[String](default = "admin", description = "username of super admin")

  var password = opt[String](default = "admin", description = "password of super admin")

  var installationDirectory = opt[String](default = "/tmp/keycloak-installation/")

  var cleanInstall = opt[Boolean](default = false, abbrev = "c",
    description = "delete current installation if exists and installs a fresh instance")

  var version = opt[String](default = "4.6.0")

  def run(): Unit ={
    println(
      s"""
         |OPTIONS:
         |
         |port: $port
         |host: $host
         |username: $username
         |password: $password
         |installationDirectory: $installationDirectory
         |cleanInstall: $cleanInstall
         |version: latest
       """.stripMargin)

    if(!keycloakInstalled) downloadBinaries()
    startServer()
  }

  def keycloakInstalled:Boolean = {
    val wd = getInstallationDirectory
    os.exists(wd)
  }

  private def getInstallationDirectory = Path(installationDirectory) / version
  private def getTarFilePath = Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"
  private def getKeycloakRoot = Path(installationDirectory) / version / s"binaries/"

  def decompress(compressed: Array[Byte]): Array[Byte] = {
    val gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))
    val output = new ArrayBuffer[Byte]()
    var totalByteCount = 0
    val bytes = new Array[Byte](1024)
    while (gzipInputStream.available() == 1) {
      val byteCount = gzipInputStream.read(bytes)
      if (byteCount > 0) {
        output ++= bytes.take(byteCount)
        totalByteCount += byteCount
      }
    }
    output.take(totalByteCount).toArray
  }

  def downloadBinaries():Unit = {
    println(s"downloading keycloak $version.Final")
    val url = s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"
    val response = requests.get(url)
    val tarBall = response.data.bytes
    //write tarball to file
    os.write(getTarFilePath, Source.BytesSource(tarBall), createFolders = true)
    println("downloaded keycloak")

    //decompress
    println(s"decompressing $getTarFilePath")
    val decompressedByteArray = decompress(tarBall)
    os.write(getKeycloakRoot, decompressedByteArray, createFolders = true)
    println(s"decompressed to $getKeycloakRoot")

  }
  def startServer():Unit = ???
}
