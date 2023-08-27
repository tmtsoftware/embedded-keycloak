package org.tmt.embedded_keycloak.impl

import os.{ProcessOutput, SubProcess}

private[embedded_keycloak] object Bash {

  def spawn(stdOut: ProcessOutput, env: Map[String, String], cmd: String*): SubProcess = {
    println(s"[Embedded-Keycloak] [os.spawn] Executing command: [${cmd.mkString(" ")}]")
    os.proc(cmd).spawn(env = env, stdout = stdOut)
  }

  def exec(out: ProcessOutput, cmd: String*): Int = {
    println(s"[Embedded-Keycloak] [os.call] Executing command: [${cmd.mkString(" ")}]")
    os.proc(cmd).call(stdout = out).exitCode
  }

}
