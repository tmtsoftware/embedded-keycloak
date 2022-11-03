package org.tmt.embedded_keycloak.impl.download

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.Source
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.FileIO
import org.tmt.embedded_keycloak.impl.download.DownloaderExtensions._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

private[embedded_keycloak] class AkkaDownloader(settings: Settings, fileIO: FileIO)(implicit system: ActorSystem) {
  import settings._

  private def isKeycloakDownloaded = os.exists(fileIO.tarFilePath)

  implicit private lazy val ec: ExecutionContext = system.dispatcher

  private def getContentLength(response: HttpResponse) =
    response match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.contentLengthOption.getOrElse(throw new RuntimeException("Content length is not provided"))
      case _                                          => throw new RuntimeException(s"Downloading failed with. status code: ${response.status}")
    }

  def download(): Unit = {
    if (alwaysDownload || !isKeycloakDownloaded) {
      println(s"[Embedded-Keycloak] Downloading keycloak from URL: [$keycloakDownloadUrl]")
      println(s"[Embedded-Keycloak] Downloading keycloak at location: [${fileIO.downloadDirectory}]")
      fileIO.deleteVersion()

      val responseFuture = AkkaHttpUtils.singleRequestWithRedirect(HttpRequest(uri = keycloakDownloadUrl))
      val contentLength  = responseFuture.map(getContentLength)

      val source: Source[DownloadProgress, Future[Done]] =
        responseFuture.toByteStringSource
          .toProgressSource(contentLength)
          .writeToFile(fileIO.incompleteTarFilePath)
          .untilDownloadCompletes
          .compressForPrinting

      val downloadCompleteF = source.runForeach(progress => print(s"\r$progress"))

      Await.result(downloadCompleteF, 20.minutes)
      fileIO.moveIncompleteFile()
      println(s"\n[Embedded-Keycloak] Keycloak downloaded successfully at location: [${fileIO.tarFilePath}]")
    }
  }
}
