package org.tmt.embedded_keycloak.impl.download

import org.scalatest.FunSuite
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.FileIO

class AkkaDownloaderTest extends FunSuite {
  test("should download keycloak") {
    val settings   = Settings(alwaysDownload = true)
    val downloader = new AkkaDownloader(settings, new FileIO(settings))
    downloader.download()
  }
}
