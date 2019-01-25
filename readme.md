# Embedded Keycloak

Embedded keycloak server for jvm integration testing. (developed in scala)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/tech.bilal/embedded-keycloak_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/tech.bilal/embedded-keycloak_2.12)
[![Build Status](https://travis-ci.com/bilal-fazlani/embedded-keycloak.svg?branch=master)](https://travis-ci.com/bilal-fazlani/embedded-keycloak)
[![Coverage Status](https://coveralls.io/repos/github/bilal-fazlani/embedded-keycloak/badge.svg?branch=master)](https://coveralls.io/github/bilal-fazlani/embedded-keycloak?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/39e38c33fbab49f996971aab557a072a)](https://www.codacy.com/app/bilal-fazlani/embedded-keycloak?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=bilal-fazlani/embedded-keycloak&amp;utm_campaign=Badge_Grade)

## Installation

```scala
libraryDependencies += "tech.bilal" %% "embedded-keycloak" % "<ADD_VERSION_HERE>"
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
         version: String = "4.6.0")
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

## Limitations

- Does not support permissions
- Only supports the configs shown in the config file
- Does not support importing keycloak exported json file