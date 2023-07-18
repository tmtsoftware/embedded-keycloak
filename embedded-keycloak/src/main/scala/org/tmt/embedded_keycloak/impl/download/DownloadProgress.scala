package org.tmt.embedded_keycloak.impl.download

import java.text.DecimalFormat

import org.apache.pekko.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

private[embedded_keycloak] case class DownloadProgress(downloadedBytes: Long, totalBytes: Long, lastChunk: ByteString) {
  def +(newData: ByteString): DownloadProgress =
    DownloadProgress(downloadedBytes = downloadedBytes + newData.length, totalBytes = totalBytes, lastChunk = newData)

  override def toString: String =
    s"${math.round(percentage)}% " +
      s"(${readableFileSize(downloadedBytes)} of ${readableFileSize(totalBytes)})"

  def percentage: Double = (downloadedBytes.toDouble / totalBytes.toDouble) * 100

  private def readableFileSize(size: Long): String = {
    if (size <= 0) "0 B"
    else {
      val units           = Array[String]("B", "kB", "MB", "GB", "TB")
      val digitGroups     = (Math.log10(size.toDouble) / Math.log10(1024)).toInt
      val decimalFormat   = new DecimalFormat("#,##0.#")
      val number          = size / Math.pow(1024, digitGroups)
      val formattedNumber = decimalFormat.format(number)
      s"$formattedNumber ${units(digitGroups)}"
    }
  }
}

private[embedded_keycloak] object DownloadProgress {
  def apply(downloadedBytes: Long, totalBytes: Long, lastChunk: ByteString): DownloadProgress =
    new DownloadProgress(downloadedBytes, totalBytes, lastChunk)

  def empty(totalBytes: Future[Long])(implicit ec: ExecutionContext): Future[DownloadProgress] =
    totalBytes.map { bytes => DownloadProgress(downloadedBytes = 0, totalBytes = bytes, lastChunk = ByteString.empty) }
}
