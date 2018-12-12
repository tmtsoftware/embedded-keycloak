package embedded.keycloak.internal

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import embedded.keycloak.models.{DownloadProgress, Settings}
import os.Path

import scala.concurrent.Future

class KeycloakInstaller(settings: Settings) {

  import settings._

  private def getUrl =
    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"
  private def getInstallationDirectory = Path(installationDirectory) / version
  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"
  private def getKeycloakRoot =
    Path(installationDirectory) / version / s"binaries/"

  private def download(progress: DownloadProgress => Unit) = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = getUrl))

    val contentLength = responseFuture.map {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.contentLengthOption
    }

    val f: Future[Source[ByteString, Any]] = responseFuture.map {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.withoutSizeLimit.dataBytes
      case HttpResponse(statusCode, _, _, _) =>
        throw new RuntimeException(
          s"failed to download file. Status code: $statusCode")
    }

    val a: Source[DownloadProgress, Future[Done]] = Source
      .fromFutureSource(f)
      .scan(0L) { (acc, bs) =>
        acc + bs.length
      }
      .scanAsync(DownloadProgress.empty(contentLength)) { (acc, current) =>
        acc.map(_ => DownloadProgress(current, contentLength))
      }
      .mapAsync(1)(identity)
      .mapMaterializedValue(matF => matF.map(_ => Done))

    val result = a.runForeach(progress)

//    result.onComplete(_ => system.terminate())

    result
  }

  private def clean(): Unit = {
    os.remove.all(getInstallationDirectory)
  }

  private def isKeycloakInstalled: Boolean = {
    val wd = getInstallationDirectory
    os.exists(wd)
  }

  def install(f: DownloadProgress => Unit) = {
    if (cleanInstall) clean()

    if (!isKeycloakInstalled) download(f)
    else Future.successful(Done)
  }
}
