package embedded.keycloak

import os.{CommandResult, proc}

object OsLibExtensions {
  implicit class RichProc(proc: proc) {
    def |(secondProc: proc): CommandResult =
      proc.call(check = false) | secondProc
  }

  implicit class RichCommandResult(commandResult: CommandResult) {
    def output: Iterator[String] = {
      commandResult.chunks.iterator
        .collect {
          case Left(s)  => s
          case Right(s) => s
        }
        .map(x => new String(x.array))
    }

    def |(proc: proc): CommandResult = {
      proc.call(stdin = commandResult.out.bytes, check = false)
    }

    def andPrint(): CommandResult = {
      commandResult.output.foreach(println)
      commandResult
    }
  }
}
