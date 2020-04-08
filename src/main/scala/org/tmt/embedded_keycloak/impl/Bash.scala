package org.tmt.embedded_keycloak.impl

import os.{ProcessOutput, SubProcess}

private[embedded_keycloak] object Bash {

  def spawn(stdOut: ProcessOutput, cmd: String*): SubProcess = {
    println(s"[os.spawn] Executing command: [${cmd.mkString(" ")}]")
    os.proc(cmd).spawn(stdout = stdOut)
  }

  def exec(out: ProcessOutput, cmd: String*): Int = {
    println(s"[os.call] Executing command: [${cmd.mkString(" ")}]")
    os.proc(cmd).call(stdout = out).exitCode
  }

}
