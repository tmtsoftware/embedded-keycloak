package embedded.keycloak.internal

import Extensions._

object Bash {
  def exec(command: String) = {
    os.proc(command.split(" "))
      .executeAndShow(true)
  }
}
