package org.tmt.embedded_keycloak.impl

import os.CommandResult

private[embedded_keycloak] object OsLibExtensions {
  implicit class RichCommandResult(commandResult: CommandResult) {
    def output: Iterator[String] = {
      commandResult.chunks.iterator
        .collect {
          case Left(s)  => s
          case Right(s) => s
        }
        .map(x => new String(x.array))
    }
  }
}
