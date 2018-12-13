package embedded.keycloak

import embedded.keycloak.commands.KeycloakCommand
import org.backuity.clist.Cli

object Main extends App {

  Cli
    .parse(args)
    .withProgramName("embedded-keycloak")
    .withCommand(new KeycloakCommand())(_.run())
}
