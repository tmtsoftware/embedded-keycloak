package tech.bilal.embedded_keycloak.impl.download

import org.scalatest.{FunSuite, Tag}
import tech.bilal.embedded_keycloak.Settings

class CurlDownloaderTest extends FunSuite {
  ignore("testDownload", Tag("slow")) {
    val downloader =
      new CurlDownloader(Settings.default.copy(alwaysDownload = true))

    downloader.download()
  }
}
