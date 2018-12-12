package embedded.keycloak.internal

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import embedded.keycloak.models.{DownloadProgress, Settings}
import os.Path

import scala.concurrent.{ExecutionContext, Future}

class Installer(settings: Settings) {

  import settings._

  private def getInstallationDirectory = Path(installationDirectory) / version

  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries"

  private def getBinDirectory =
    getKeycloakRoot / s"keycloak-$version.Final" / "bin"

  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"

  private def getUrl =
    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

  private def clean(): Unit = {
    os.remove.all(getInstallationDirectory)
  }

  private def isKeycloakInstalled: Boolean = {
    val wd = getInstallationDirectory
    os.exists(wd)
  }

  private def download(progress: DownloadProgress => Unit) = {

    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = getUrl))

    val contentLength = responseFuture.map {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.contentLengthOption
    }

    import Extensions._

    val source: Source[DownloadProgress, Future[Done]] =
      responseFuture.toByteStringSource
        .toProgressSource(contentLength)
        .writeToFile(getTarFilePath)
        .untilDownloadCompletes

    val materializedValue = source.runForeach(progress)

    materializedValue.onComplete(_ => system.terminate())

    materializedValue
  }

  private def decompress(): Unit = {

    os.makeDir.all(getKeycloakRoot)

    val commandResult = os
      .proc("tar", "-xzf", getTarFilePath, "-C", getKeycloakRoot)
      .call(cwd = getInstallationDirectory)
    if (commandResult.exitCode != 0)
      throw new RuntimeException(
        s"could not decompress keycloak tar file. exit code ${commandResult.exitCode}")
  }

  private def addAdmin(): Unit = {
    val result = os
      .proc("sh",
            getBinDirectory / "add-user-keycloak.sh",
            "--user",
            username,
            "-p",
            password)
      .call()
    if (result.exitCode != 0)
      throw new RuntimeException(
        "could not add admin user. " +
          s"exit code ${result.exitCode}")
  }

  def install(progress: DownloadProgress => Unit)(
      implicit ec: ExecutionContext): Future[Unit] = {
    if (cleanInstall) clean()

    if (!isKeycloakInstalled) {

      download(progress).map(_ => {
        decompress()
        addAdmin()
      })

    } else {
      Future.successful(())
    }
  }
}
