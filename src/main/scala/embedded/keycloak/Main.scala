package embedded.keycloak

import org.backuity.clist.Cli

object Main extends App {
  Cli.parse(args)
    .withProgramName("embedded-keycloak")
    .withCommand(new Keycloak())(_.run())
}
