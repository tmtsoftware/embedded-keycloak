package org.tmt.embedded_keycloak.utils

import org.tmt.embedded_keycloak.impl.OsLibExtensions._
import os.proc

class Ports {

  private[embedded_keycloak] def checkAvailability(
      port: Int,
      throwOnError: Boolean = false): Boolean = {
    val commandResult =
      proc("lsof", "-n", s"-i4TCP:$port") | proc("grep", "LISTEN")

    val free = commandResult.output.isEmpty

    if (throwOnError && !free) {
      val (processName, pid) = getProcessForPort(port)
      throw new RuntimeException(
        s"port $port is not available. a $processName process with " +
          s"pid $pid is listening on port $port")
    }
    free
  }

  def stop(port: Long): Unit = {
    proc("lsof", "-n", s"-i4TCP:$port") |
      proc("grep", "LISTEN") |
      proc("awk", "{print $2}") |
      proc("xargs", "kill", "-9")
  }

  private def getProcessForPort(port: Int) = {
    val processName = (proc("lsof", "-n", s"-i4TCP:$port") |
      proc("grep", "LISTEN") |
      proc("awk", "{print $1}")).output.toList.head.trim

    val pid = (proc("lsof", "-n", s"-i4TCP:$port") |
      proc("grep", "LISTEN") |
      proc("awk", "{print $2}")).output.toList.head.trim

    (processName, pid)
  }
}
