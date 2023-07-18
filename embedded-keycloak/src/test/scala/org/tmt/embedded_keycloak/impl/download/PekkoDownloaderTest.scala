package org.tmt.embedded_keycloak.impl.download

import org.apache.pekko.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.FileIO

class PekkoDownloaderTest extends AnyFunSuite with BeforeAndAfterAll with ScalaFutures with Matchers {
  implicit private lazy val system: ActorSystem = ActorSystem("test")

  override protected def afterAll(): Unit = system.terminate().futureValue

  test("should download keycloak") {
    val settings   = Settings(alwaysDownload = true)
    val io         = new FileIO(settings)
    val downloader = new PekkoDownloader(settings, io)
    downloader.download()
    io.isKeycloakDownloaded shouldBe true
  }
}
