package org.tmt.embedded_keycloak.impl

import os.{Path, SubProcess, proc}

private[embedded_keycloak] object Bash {

  implicit class RichProc(proc: proc) {
    def executeAndShow(throwOnError: Boolean = false, cwd: Path = null): Int = { // scalastyle:ignore
      val exitCode = proc.stream(
        cwd = cwd,
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
        throw new RuntimeException(
          s"the command $proc resulted in exit code $exitCode")

      exitCode
    }

    def executeBackground(cwd: Path = null): SubProcess = { // scalastyle:ignore
      proc.spawn(cwd = cwd)
    }
  }

  /**
    * Execute on current thread
    * @param command
    * @param cwd
    * @return
    */
  def exec(command: String, cwd: Path = null) = { // scalastyle:ignore
    os.proc(command.split(" "))
      .executeAndShow(throwOnError = true, cwd = cwd)
  }

  /**
    * Execute in background
    * @param command
    * @param cwd
    * @return
    */
  def background(command: String, cwd: Path = null) = { // scalastyle:ignore
    os.proc(command.split(" "))
      .executeBackground(cwd = cwd)
  }
}
