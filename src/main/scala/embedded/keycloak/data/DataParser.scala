package embedded.keycloak.data

import com.typesafe.config.ConfigFactory
import embedded.keycloak.models.KeycloakData
import net.ceedubs.ficus.Ficus.toFicusConfig
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

object DataParser {
  def parse: KeycloakData = {
    val config = ConfigFactory
      .load()
      .getConfig("embedded-keycloak")
      .getConfig("data")
    config.as[KeycloakData]
  }
}
