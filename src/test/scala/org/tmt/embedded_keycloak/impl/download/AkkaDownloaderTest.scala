package org.tmt.embedded_keycloak.impl.download

import org.scalatest.funsuite.AnyFunSuite
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.FileIO

class AkkaDownloaderTest extends AnyFunSuite {
  test("should download keycloak") {
    val settings   = Settings(alwaysDownload = true)
    val downloader = new AkkaDownloader(settings, new FileIO(settings))
    downloader.download()
  }
}
