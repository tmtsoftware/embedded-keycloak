package org.tmt.embedded_keycloak.impl.download

import org.scalatest.FunSuite
import org.tmt.embedded_keycloak.Settings

class AkkaDownloaderTest extends FunSuite {
  test("should download keycloak") {
    val downloader =
      new AkkaDownloader(Settings(alwaysDownload = true))

    downloader.download()
  }
}
