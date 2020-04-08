package org.tmt.embedded_keycloak.utils

import java.net.Socket

import scala.util.Try

object Ports {
  def isFree(port: Int): Boolean = Try(new Socket("localhost", port)).map(_.close()).isFailure
}
