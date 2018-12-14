package embedded.keycloak.download

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import embedded.keycloak.models.Settings
import os.Path
import DownloaderExtensions._
import embedded.keycloak.internal.DownloadProgress

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class AkkaDownloader(settings: Settings)(implicit actorSystem: ActorSystem) {

  implicit val ec = actorSystem.dispatcher
  import settings._

  private def getInstallationDirectory = Path(installationDirectory) / version

  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"

  private def getUrl =
    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

  private def isKeycloakDownloaded: Boolean = {
    os.exists(getTarFilePath)
  }

  private def cleanEverything(): Unit = {
    os.remove.all(getInstallationDirectory)
  }

  def download(): Unit = {
    if (alwaysDownload || !isKeycloakDownloaded) {
      cleanEverything()

      implicit val materializer: ActorMaterializer = ActorMaterializer()

      val responseFuture: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = getUrl))

      val contentLength = responseFuture.map {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.contentLengthOption
      }

      val source: Source[DownloadProgress, Future[Done]] =
        responseFuture.toByteStringSource
          .toProgressSource(contentLength)
          .writeToFile(getTarFilePath)
          .untilDownloadCompletes

      val materializedValue = source.runForeach(x => print(s"\r$x"))

      Await.result(materializedValue, 5.minutes)
    }
  }
}
