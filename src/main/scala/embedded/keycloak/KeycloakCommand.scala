package embedded.keycloak

import embedded.keycloak.internal.KeycloakInstaller
import embedded.keycloak.models.Settings
import org.backuity.clist._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class KeycloakCommand extends Command(description = "starts keycloak server") {

  var port = opt[Int](default = 8081,
                      description =
                        "port number to use for keycloak http server")

  var host = opt[String](default = "0.0.0.0", description = "address to bind")

  var username =
    opt[String](default = "admin", description = "username of super admin")

  var password =
    opt[String](default = "admin", description = "password of super admin")

  var installationDirectory =
    opt[String](default = "/tmp/keycloak-installation/")

  var cleanInstall = opt[Boolean](
    default = false,
    abbrev = "c",
    description =
      "delete current installation if exists and installs a fresh instance")

  var version = opt[String](default = "4.6.0")

  private def settings =
    Settings(port,
             host,
             username,
             password,
             installationDirectory,
             cleanInstall,
             version)

  def run(): Unit = {
    println(s"""
         |OPTIONS:
         |
         |port: $port
         |host: $host
         |username: $username
         |password: $password
         |installationDirectory: $installationDirectory
         |cleanInstall: $cleanInstall
         |version: $version
       """.stripMargin)

    val installer = new KeycloakInstaller(settings)

    Await.result(installer.install(x => print(s"\r$x")), 10.minutes)

//    if (cleanInstall) clean()
//
//    if (!keycloakInstalled) {
//      download(getUrl)
//    }

//    decompress()
//
//    startServer()
  }

//  def clean(): Unit = {
//    os.remove.all(getInstallationDirectory)
//  }
//
//  def keycloakInstalled: Boolean = {
//    val wd = getInstallationDirectory
//    os.exists(wd)
//  }

//  private def getUrl =
//    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"
//  private def getInstallationDirectory = Path(installationDirectory) / version
//  private def getTarFilePath =
//    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"
//  private def getKeycloakRoot =
//    Path(installationDirectory) / version / s"binaries/"

//  def decompress(compressed: Array[Byte]): Array[Byte] = {
//    val gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))
//    val output = new ArrayBuffer[Byte]()
//    var totalByteCount = 0
//    val bytes = new Array[Byte](1024)
//    while (gzipInputStream.available() == 1) {
//      val byteCount = gzipInputStream.read(bytes)
//      if (byteCount > 0) {
//        output ++= bytes.take(byteCount)
//        totalByteCount += byteCount
//      }
//    }
//    output.take(totalByteCount).toArray
//  }

//  def downloadBinaries():Unit = {
//    println(s"downloading keycloak $version.Final")
//    val url = s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"
//    val response = requests.get(url)
//    val tarBall = response.data.bytes
//    //write tarball to file
//    os.write(getTarFilePath, Source.BytesSource(tarBall), createFolders = true)
//    println("downloaded keycloak")
//
//    //decompress
//    println(s"decompressing $getTarFilePath")
//    val decompressedByteArray = decompress(tarBall)
//    os.write(getKeycloakRoot, decompressedByteArray, createFolders = true)
//    println(s"decompressed to $getKeycloakRoot")
//  }

//  def decompress(): Unit ={
//    println(s"decompressing $getTarFilePath")
//    val decompressedByteArray = decompress(os.read(getTarFilePath).getBytes())
//    os.write(getKeycloakRoot, decompressedByteArray, createFolders = true)
//    println(s"decompressed to $getKeycloakRoot")
//  }

//  def download1(url: String): Unit = {
//    implicit val system = ActorSystem()
//    implicit val materializer = ActorMaterializer()
//    implicit val executionContext = system.dispatcher
//
//    val responseFuture: Future[HttpResponse] =
//      Http().singleRequest(HttpRequest(uri = getUrl))
//  }
//
//  def download(url: String): Unit = {
//    println("starting download")
//    requests.get.stream(url)(
//      onHeadersReceived = _ => {
//        println("download started")
//      },
//      onDownload = inStream => {
//        os.write(getTarFilePath,
//                 Source.InputStreamSource(inStream),
//                 createFolders = true)
//        println("downloaded file")
//      }
//    )
//  }
//
//  def startServer(): Unit = ()
}
