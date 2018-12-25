package tech.bilal.embedded_keycloak.impl.download

import org.scalatest.FunSuite
import tech.bilal.embedded_keycloak.Settings

class AkkaDownloaderTest extends FunSuite {
  test("should download keycloak") {
    val downloader =
      new AkkaDownloader(Settings.default.copy(alwaysDownload = true))

    downloader.download()
  }
}
