package org.tmt.embedded_keycloak.impl.download

import akka.Done
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

private[embedded_keycloak] object DownloaderExtensions {

  implicit class ProgressSource(source: Source[DownloadProgress, Future[Done]]) {
    def writeToFile(path: os.Path): Source[DownloadProgress, Future[Done]] = {
      source.map { progress =>
        val osSource =
          os.Source.BytesSource(progress.lastChunk.toArray)
        os.write.append(path, osSource, createFolders = true)
        progress
      }
    }

    def untilDownloadCompletes: Source[DownloadProgress, Future[Done]] =
      source.takeWhile(p => p.downloadedBytes <= p.totalBytes)

    def compressForPrinting: Source[DownloadProgress, Future[Done]] = {
      source
        .statefulMapConcat(() => {
          var lastProgressPercentage = 0D
          thisProgress =>
            if (thisProgress.percentage - lastProgressPercentage > 10D | thisProgress.percentage == 100D | thisProgress.percentage == 0D) {
              lastProgressPercentage = thisProgress.percentage
              List(thisProgress)
            } else {
              List.empty[DownloadProgress]
            }
        })
    }
  }

  implicit class RichByteStringSourceOfDone(source: Source[ByteString, Future[Done]]) {
    def toProgressSource(contentLength: Future[Long])(implicit ec: ExecutionContext): Source[DownloadProgress, Future[Done]] = {
      source
        .scan(DownloadProgress.empty(contentLength)) { (lastProgressF, currentData) =>
          lastProgressF.map(lastProgress => lastProgress + currentData)
        }
        .mapAsync(1)(identity)
    }
  }

  implicit class RichByteStringSourceOfAny(sourceF: Source[ByteString, Future[Any]]) {
    def addMaterializer(implicit ec: ExecutionContext): Source[ByteString, Future[Done]] = {
      sourceF
        .mapMaterializedValue(matF => matF.map(_ => Done))
    }
  }

  implicit class RichHttpResponseFuture(responseF: Future[HttpResponse])(implicit ec: ExecutionContext) {
    def toByteStringSource: Source[ByteString, Future[Done]] = {
      Source
        .futureSource(
          responseF
            .map {
              case HttpResponse(StatusCodes.OK, _, entity, _) =>
                entity.withoutSizeLimit.dataBytes
              case HttpResponse(statusCode, _, _, _) =>
                throw new RuntimeException(s"failed to download keycloak. Status code: $statusCode")
            }
        )
        .addMaterializer
    }
  }
}
