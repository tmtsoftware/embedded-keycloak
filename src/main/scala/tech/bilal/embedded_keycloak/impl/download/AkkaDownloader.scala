package tech.bilal.embedded_keycloak.impl.download

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import os.Path
import tech.bilal.embedded_keycloak.Settings
import tech.bilal.embedded_keycloak.impl.download.DownloadProgress.DownloadProgressWithTotalLength
import tech.bilal.embedded_keycloak.impl.download.DownloaderExtensions._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

private[embedded_keycloak] class AkkaDownloader(settings: Settings) {

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
    println("downloading keycloak...")

    if (alwaysDownload || !isKeycloakDownloaded) {
      cleanEverything()

      val config = ConfigFactory
        .load()
        .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
        .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))

      implicit val actorSystem = ActorSystem("download-actor-system", config)
      implicit val ec = actorSystem.dispatcher
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
          .compressForPrinting

      val materializedValue =
        source.runForeach {
          case progress: DownloadProgressWithTotalLength =>
            print(s"\r${math.ceil(progress.percentage).toInt} %")
        }

      materializedValue.onComplete(_ => actorSystem.terminate())

      Await.result(materializedValue, 20.minutes)
    }
  }
}
