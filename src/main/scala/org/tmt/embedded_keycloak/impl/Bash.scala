package org.tmt.embedded_keycloak.impl

import os.proc

private[embedded_keycloak] object Bash {

  implicit class RichProc(proc: proc) {
    def executeAndShow(throwOnError: Boolean = false): Int = {
      val exitCode = proc.stream(
        onOut = (buffer, length) =>
          buffer
            .slice(0, length)
            .map(x => x.toChar)
            .mkString
            .split("\n")
            .foreach(println),
        onErr = (buffer, length) =>
          buffer
            .slice(0, length)
            .map(x => x.toChar)
            .mkString
            .split("\n")
            .foreach(System.err.println)
      )

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
