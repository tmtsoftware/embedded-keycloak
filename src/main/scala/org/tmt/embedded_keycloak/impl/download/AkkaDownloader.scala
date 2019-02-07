package org.tmt.embedded_keycloak.impl.download

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.FileIO
import org.tmt.embedded_keycloak.impl.download.DownloaderExtensions._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

private[embedded_keycloak] class AkkaDownloader(settings: Settings)
    extends Downloader {

  import settings._

  val fileIO = new FileIO(settings)

  private def getUrl =
//    s"http://localhost:9090/keycloak-4.6.0.Final.tar.gz"
    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

  private def isKeycloakDownloaded: Boolean = os.exists(fileIO.tarFilePath)

  def download(): Unit = {
    if (alwaysDownload || !isKeycloakDownloaded) {
      println("downloading keycloak...")
      fileIO.deleteVersion()

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
          entity.contentLengthOption.getOrElse(
            throw new RuntimeException("content length is not provided"))
        case HttpResponse(statusCode, _, _, _) =>
          throw new RuntimeException(
            s"ERROR: error while downloading. status code: $statusCode")
      }

      val source: Source[DownloadProgress, Future[Done]] =
        responseFuture.toByteStringSource
          .toProgressSource(contentLength)
          .writeToFile(fileIO.incompleteTarFilePath)
          .untilDownloadCompletes
          .compressForPrinting

      val materializedValue =
        source.runForeach { progress =>
          print(s"\r$progress")
        }

      materializedValue.onComplete(_ => {
        actorSystem.terminate()
        fileIO.moveIncompleteFile()
        println()
        println("keycloak downloaded")
      })

      Await.result(materializedValue, 20.minutes)
    }
  }
}
