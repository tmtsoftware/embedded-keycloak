package org.tmt.embedded_keycloak.impl

import org.tmt.embedded_keycloak.impl.OsLibExtensions._
import os.{proc, SubProcess}

class StopHandle private[embedded_keycloak] (subProcess: SubProcess) {
  def stop(): Unit = {
    val process: Process = subProcess.wrapped

//    (getChildPids andThen killAll)(process.pid())
    killAll(List(process.pid()))

    subProcess.destroyForcibly()
  }

  private val killAll: List[Long] => Unit = _.foreach(proc("kill", "-9", _).call())

  private val getChildPids: Long => List[Long] = proc("pgrep", "-P", _).call().output.map(_.trim.toLong).toList

}
