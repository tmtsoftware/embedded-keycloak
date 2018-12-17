package tech.bilal.embedded_keycloak.impl.download

import org.scalatest.FunSuite
import tech.bilal.embedded_keycloak.Settings

class CurlDownloaderTest extends FunSuite {
  test("should download keycloak") {
    val downloader =
      new CurlDownloader(Settings.default.copy(alwaysDownload = true))

    downloader.download()
  }
}
