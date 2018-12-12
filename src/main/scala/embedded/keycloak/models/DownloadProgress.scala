package embedded.keycloak.models

import scala.concurrent.{ExecutionContext, Future}

sealed trait DownloadProgress {
  val downloadedBytes: Long

  def +(value: Long) = DownloadProgress(downloadedBytes + value)
}

object DownloadProgress {

  case class DownloadProgressWithTotalLength(downloadedBytes: Long,
                                             totalBytes: Long)
      extends DownloadProgress {

    import java.text.DecimalFormat

    private def readableFileSize(size: Long): String = {
      if (size <= 0) return "0"
      val units = Array[String]("B", "kB", "MB", "GB", "TB")
      val digitGroups = (Math.log10(size) / Math.log10(1024)).toInt
      new DecimalFormat("#,##0.#")
        .format(size / Math.pow(1024, digitGroups)) + " " + units(digitGroups)
    }

    override def toString =
      s"${readableFileSize(downloadedBytes)} of ${readableFileSize(totalBytes)}"
  }

  case class DownloadProgressWithoutTotalLength(downloadedBytes: Long)
      extends DownloadProgress {
    val progress: String = s"$downloadedBytes bytes"
  }

  def apply(downloadedBytes: Long): DownloadProgress =
    DownloadProgressWithoutTotalLength(downloadedBytes)
  def apply(downloadedBytes: Long, totalBytes: Long): DownloadProgress =
    DownloadProgressWithTotalLength(downloadedBytes, totalBytes)

  def apply(downloadedBytes: Long, totalBytes: Future[Option[Long]])(
      implicit ec: ExecutionContext): Future[DownloadProgress] =
    totalBytes.map {
      case Some(bytes) =>
        DownloadProgressWithTotalLength(downloadedBytes, bytes)
      case None => DownloadProgressWithoutTotalLength(downloadedBytes)
    }

  def empty(totalBytes: Future[Option[Long]])(
      implicit ec: ExecutionContext): Future[DownloadProgress] =
    totalBytes.map {
      case Some(bytes) =>
        DownloadProgressWithTotalLength(0, bytes)
      case None => DownloadProgressWithoutTotalLength(0)
    }

}
