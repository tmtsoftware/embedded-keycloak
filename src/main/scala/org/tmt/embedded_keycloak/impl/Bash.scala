package org.tmt.embedded_keycloak.impl

import os.proc

private[embedded_keycloak] object Bash {

  implicit class RichProc(proc: proc) {
    def executeAndShow(throwOnError: Boolean = false): Int = {
      val exitCode = proc.call().exitCode

      if (throwOnError && exitCode != 0)
        throw new RuntimeException(s"the command $proc resulted in exit code $exitCode")

      exitCode
    }
  }

  /**
   * Execute on current thread
   */
  def exec(cmd: String*): Int = os.proc(cmd).executeAndShow(throwOnError = true)

}
