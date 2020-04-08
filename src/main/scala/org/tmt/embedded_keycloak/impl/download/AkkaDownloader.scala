package org.tmt.embedded_keycloak.impl.download

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.FileIO
import org.tmt.embedded_keycloak.impl.download.DownloaderExtensions._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

private[embedded_keycloak] class AkkaDownloader(settings: Settings, fileIO: FileIO) extends Downloader {

  import settings._

  private def getUrl = s"https://downloads.jboss.org/keycloak/$version/keycloak-$version.tar.gz"

  private def isKeycloakDownloaded: Boolean = os.exists(fileIO.tarFilePath)

  def download(): Unit = {
    if (alwaysDownload || !isKeycloakDownloaded) {
      println("downloading keycloak...")
      fileIO.deleteVersion()

      val config = ConfigFactory.load()

      implicit val actorSystem: ActorSystem = ActorSystem("download-actor-system", config)
      implicit val ec: ExecutionContext     = actorSystem.dispatcher

      def terminateActorSystem() = Await.result(actorSystem.terminate(), 5.seconds)

      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = getUrl))

      val contentLength = responseFuture.map {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.contentLengthOption.getOrElse {
            terminateActorSystem()
            throw new RuntimeException("content length is not provided")
          }
        case HttpResponse(statusCode, _, _, _) =>
          terminateActorSystem()
          throw new RuntimeException(s"ERROR: error while downloading. status code: $statusCode")
      }

      val source: Source[DownloadProgress, Future[Done]] =
        responseFuture.toByteStringSource
          .toProgressSource(contentLength)
          .writeToFile(fileIO.incompleteTarFilePath)
          .untilDownloadCompletes
          .compressForPrinting

      val downloadCompleteF = source.runForeach(progress => print(s"\r$progress"))

      Await.result(downloadCompleteF, 20.minutes)
      Await.result(actorSystem.terminate(), 5.seconds)
      fileIO.moveIncompleteFile()
      println()
      println("keycloak downloaded")
    }
  }
}
