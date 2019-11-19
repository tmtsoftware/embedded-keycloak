package org.tmt.embedded_keycloak.utils

import java.net.Socket

import org.tmt.embedded_keycloak.impl.OsLibExtensions._
import os.proc

import scala.util.Try

object Ports {

  private[embedded_keycloak] def checkAvailability(
    port: Int,
    throwOnError: Boolean = false
  ): Boolean = {
    val free = isFree(port = port)
    if (!free && throwOnError)
      throw new RuntimeException(s"port $port is not available.")
    free
  }

  def stop(port: Long): Unit = {
    proc("lsof", "-n", s"-i4TCP:$port") |
      proc("grep", "LISTEN") |
      proc("awk", "{print $2}") |
      proc("xargs", "kill", "-9")
  }

  def isFree(host: String = "localhost", port: Int): Boolean =
    Try(new Socket(host, port)).map(_.close()).isFailure

}
