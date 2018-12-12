package embedded.keycloak.internal

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import embedded.keycloak.models.{DownloadProgress, Settings}
import os.Path

import scala.concurrent.Future

class Downloader(settings: Settings) {

  import settings._

  private def getTarFilePath =
    Path(installationDirectory) / version / s"keycloak-$version.Final.tar.gz"

  private def getUrl =
    s"https://downloads.jboss.org/keycloak/$version.Final/keycloak-$version.Final.tar.gz"

  def download(): Future[_] = {

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

    val materializedValue = source.runForeach(x => print(s"\r$x"))

    materializedValue.onComplete(_ => system.terminate())

    materializedValue
  }
}
