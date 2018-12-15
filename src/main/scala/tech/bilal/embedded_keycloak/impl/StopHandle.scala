package tech.bilal.embedded_keycloak.impl

import os.{SubProcess, proc}
import tech.bilal.embedded_keycloak.impl.OsLibExtensions._

private[embedded_keycloak] class StopHandle(subProcess: SubProcess) {
  def stop(): Unit = {
    val process: Process = subProcess.wrapped.asInstanceOf[java.lang.Process]
    val pid = getPidOfProcess(process)
    getAllChildPids(pid).foreach(killPid)
    subProcess.destroyForcibly()
  }

  private def killPid(pid: Long): Unit = proc("kill", "-9", pid).call()

  private def getAllChildPids(pid: Long): List[Long] =
    proc("pgrep", "-P", pid).call().output.map(_.trim.toLong).toList

  private def getPidOfProcess(p: Process): Long = {
    var pid: Long = -1
    try if (p.getClass.getName == "java.lang.UNIXProcess") {
      val f = p.getClass.getDeclaredField("pid")
      f.setAccessible(true)
      pid = f.getLong(p)
      f.setAccessible(false)
    } catch {
      case e: Exception =>
        pid = -1
    }
    pid
  }
}
