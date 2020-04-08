package org.tmt.embedded_keycloak.impl
import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.impl.data.DataFeeder

private[embedded_keycloak] class Wiring(settings: Settings) {

  lazy val healthCheck = new HealthCheck(settings)
  lazy val dataFeeder  = new DataFeeder(settings)
  lazy val fileIO      = new FileIO(settings)
  lazy val installer   = new Installer(settings, fileIO)

}
