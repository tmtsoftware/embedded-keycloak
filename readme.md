# Embedded Keycloak

Embedded keycloak server for jvm integration testing. (developed in scala)

[![Build Status](https://github.com/tmtsoftware/embedded-keycloak/workflows/CI/badge.svg)

## Installation

add the resolver: 

```scala
resolvers += "jitpack" at "https://jitpack.io"
```

add the dependency:
```scala
libraryDependencies += "org.tmt" %% "embedded-keycloak" % "<ADD_LATEST_VERSION_HERE>"
```

## Usage

```scala
val keycloak = new EmbeddedKeycloak(
      KeycloakData.fromConfig, // or directly: `KeycloakData(...)`
      Settings.default) // or customize: Settings(...)

val stopHandle = await(keycloak.startServerInBackground())

//do some testing here

stopHandle.stop()
```

## Settings

the following settings options are available. 

The **default settings** looks like this -

```scala
Settings(port: Int = 8081,
         host: String = "0.0.0.0",
         keycloakDirectory: String = "/tmp/embedded-keycloak/",
         cleanPreviousData: Boolean = true,
         alwaysDownload: Boolean = false,
         version: String = "8.0.1")
```

## Keycloak Data

The test data can be provided in application.conf of test scope.

For example -

```hocon
embedded-keycloak{
    adminUser {
      username = admin
      password = admin
    }
    realms = [{
      name = example-realm
      realmRoles = [super-admin]
      clients = [
        {
          name = some-server
          clientType = bearer-only
          clientRoles = [server-admin, server-user]
          authorizationEnabled = true
        },
        {
          name = some-client
        }
      ]
      users = [
        {
          username = user1
          password = abcd,
          realmRoles = [super-admin]
          firstName = john
        },
        {
          username = user2
          password = abcd,
          clientRoles = [{
            clientName = some-server
            roleName = server-user
          }]
      }]
    }]
}
```

Or the same data can be provided directly as shown below:

```scala
    val data = KeycloakData(
      adminUser = AdminUser("admin", "admin"),
      realms = Set(
        Realm(
          name = "example-realm",
          clients = Set(
            Client(
              name = "some-server",
              clientType = "bearer-only",
              clientRoles = Set("server-admin", "server-user"),
              authorizationEnabled = true
            ),
            Client(name = "some-client")
          ),
          users = Set(
            ApplicationUser(
              username = "user1",
              firstName = "john",
              password = "abcd",
              realmRoles = Set("super-admin")
            ),
            ApplicationUser(
              username = "user2",
              password = "abcd",
              clientRoles = Set(ClientRole("some-server", "server-user"))
            )
          ),
          realmRoles = Set("super-admin")
        ))
    )
``` 
