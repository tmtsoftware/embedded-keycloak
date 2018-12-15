package tech.bilal.embedded_keycloak.impl

import os.proc
import tech.bilal.embedded_keycloak.impl.OsLibExtensions._

private[embedded_keycloak] class Ports {

  def checkAvailability(port: Int, `throw`: Boolean = false): Boolean = {
    val commandResult =
      proc("lsof", "-n", s"-i4TCP:$port") | proc("grep", "LISTEN")

    val free = commandResult.output.isEmpty

    if (`throw` && !free) {
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
