package org.tmt.embedded_keycloak.impl
import akka.actor.ActorSystem
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.data.DataFeeder
import org.tmt.embedded_keycloak.impl.download.AkkaDownloader

private[embedded_keycloak] class Wiring(settings: Settings) {
  implicit lazy val actorSystem: ActorSystem = ActorSystem("embedded-keycloak")

  lazy val healthCheck        = new HealthCheck(settings)
  lazy val dataFeeder         = new DataFeeder(settings)
  lazy val fileIO             = new FileIO(settings)
  private lazy val downloader = new AkkaDownloader(settings, fileIO)
  lazy val installer          = new Installer(settings, fileIO, downloader)
}
