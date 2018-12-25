package tech.bilal.embedded_keycloak.impl.download

import java.text.DecimalFormat

import akka.util.ByteString
import DownloadProgress.{
  DownloadProgressWithTotalLength,
  DownloadProgressWithoutTotalLength
}

import scala.concurrent.{ExecutionContext, Future}

private[embedded_keycloak] sealed trait DownloadProgress {
  val downloadedBytes: Long

  private[embedded_keycloak] val lastChunk: ByteString

  def +(value: ByteString): DownloadProgress = this match {
    case DownloadProgressWithTotalLength(`downloadedBytes`, totalBytes, _) =>
      DownloadProgress(`downloadedBytes` + value.length, totalBytes, value)
    case DownloadProgressWithoutTotalLength(`downloadedBytes`, _) =>
      DownloadProgress(`downloadedBytes` + value.length, value)
  }

  protected def readableFileSize(size: Long): String = {
    if (size <= 0) return "0"
    val units = Array[String]("B", "kB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size) / Math.log10(1024)).toInt
    new DecimalFormat("#,##0.#")
      .format(size / Math.pow(1024, digitGroups)) + " " + units(digitGroups)
  }
}

private[embedded_keycloak] object DownloadProgress {

  case class DownloadProgressWithTotalLength(downloadedBytes: Long,
                                             totalBytes: Long,
                                             lastChunk: ByteString)
      extends DownloadProgress {

    override def toString =
      s"${readableFileSize(downloadedBytes)} of ${readableFileSize(totalBytes)}"

    def percentage: Double =
      (downloadedBytes.toDouble / totalBytes.toDouble) * 100
  }

  case class DownloadProgressWithoutTotalLength(downloadedBytes: Long,
                                                lastChunk: ByteString)
      extends DownloadProgress {
    val progress: String = s"$downloadedBytes bytes"

    override def toString =
      s"${readableFileSize(downloadedBytes)}"
  }

  def apply(downloadedBytes: Long, lastChunk: ByteString): DownloadProgress =
    DownloadProgressWithoutTotalLength(downloadedBytes, lastChunk)

  def apply(downloadedBytes: Long,
            totalBytes: Long,
            lastChunk: ByteString): DownloadProgress =
    DownloadProgressWithTotalLength(downloadedBytes, totalBytes, lastChunk)

  def apply(downloadedBytes: Long,
            totalBytes: Future[Option[Long]],
            lastChunk: ByteString)(
      implicit ec: ExecutionContext): Future[DownloadProgress] =
    totalBytes.map {
      case Some(bytes) =>
        DownloadProgressWithTotalLength(downloadedBytes,
                                        bytes,
                                        lastChunk: ByteString)
      case None =>
        DownloadProgressWithoutTotalLength(downloadedBytes, lastChunk)
    }

  def empty(totalBytes: Future[Option[Long]])(
      implicit ec: ExecutionContext): Future[DownloadProgress] =
    totalBytes.map {
      case Some(bytes) =>
        DownloadProgressWithTotalLength(0, bytes, ByteString.empty)
      case None => DownloadProgressWithoutTotalLength(0, ByteString.empty)
    }

}
