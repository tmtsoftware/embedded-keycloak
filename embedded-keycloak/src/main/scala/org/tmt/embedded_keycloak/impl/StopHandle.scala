package org.tmt.embedded_keycloak.impl

import org.tmt.embedded_keycloak.impl.OsLibExtensions._
import org.tmt.embedded_keycloak.utils.Ports
import os.{proc, SubProcess}

class StopHandle private[embedded_keycloak] (subProcess: SubProcess, port: Int) {
  def stop(): Unit = {
    val process: Process = subProcess.wrapped

    (getChildPids andThen killAll)(process.pid())

    subProcess.destroyForcibly()
    Ports.stop(port)
  }

  private val killAll: List[Long] => Unit = _.foreach(proc("kill", "-9", _).call())

  private val getChildPids: Long => List[Long] = proc("pgrep", "-P", _).call().output.map(_.trim.toLong).toList

}
