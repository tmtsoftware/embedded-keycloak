package embedded.keycloak.internal

import os.{Path, SubProcess, proc}

object Bash {

  implicit class RichProc(proc: proc) {
    def executeAndShow(`throw`: Boolean = false, cwd: Path = null): Int = {
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

      if (`throw` && exitCode != 0)
        throw new RuntimeException(
          s"the command $proc resulted in exit code $exitCode")

      exitCode
    }

    def executeBackground(cwd: Path = null): SubProcess = {
      val p = proc.spawn(cwd = cwd)

      p
    }
  }

  def exec(command: String, cwd: Path = null) = {
    os.proc(command.split(" "))
      .executeAndShow(`throw` = true, cwd = cwd)
  }

  def background(command: String, cwd: Path = null) = {
    os.proc(command.split(" "))
      .executeBackground(cwd = cwd)
  }
}