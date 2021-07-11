# keycloak-client-authz

[![build](https://github.com/hoo29/keycloak-client-authz/actions/workflows/build.yml/badge.svg)](https://github.com/hoo29/keycloak-client-authz/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hoo29_keycloak-client-authz&metric=alert_status)](https://sonarcloud.io/dashboard?id=hoo29_keycloak-client-authz)
[![Open in Visual Studio Code](https://open.vscode.dev/badges/open-in-vscode.svg)](https://open.vscode.dev/hoo29/keycloak-client-authz)

Keycloak authenticator plugin for client authorisation. Allows using membership of client roles to enforce authorisation during Keycloak login.

Keycloak's built in authorisation services only provide for evaluation of policies but delegates enforcement to the clients themselves. For COTS software and SPA web apps this isn't always feasible.

# versioning

The plugin releases are formed of two versions `x.x.x-y.y.y` where `x.x.x` is they Keycloak version this plugin was built against and `y.y.y` is the semantic version of the plugin. Details of the plugin version can be found in the [CHANGELOG](./CHANGELOG.md).

The Keycloak version does not change the plugin jar itself as no Keycloak components are bundled. All of the dependencies have a `scope` of `provided`. Building against a specific version is used as a means to check the Keycloak SPI has not changed.

This plugin will likely work with any Keycloak version as the Authenticator SPI is fairly stable, although still marked as internal.

# installation

The compiled plugin is available at the project's [releases page](https://github.com/hoo29/keycloak-client-authz/releases).

The are several options to install this plugin depending on how you are running Keycloak. Below are a couple of examples. For more information, see the official [Keycloak docs](https://www.keycloak.org/docs/latest/server_development/#registering-provider-implementations).

## custom docker image

You can extend the Keycloak container image with a `Dockerfile` like:

```dockerfile
ARG KEYCLOAK_VERSION

FROM quay.io/keycloak/keycloak:$KEYCLOAK_VERSION

ARG PLUGIN_VERSION
ARG KEYCLOAK_VERSION

ADD --chown=1000:0 "https://github.com/hoo29/keycloak-client-authz/releases/download/${KEYCLOAK_VERSION}-${PLUGIN_VERSION}/${KEYCLOAK_VERSION}-${PLUGIN_VERSION}.jar" "/opt/jboss/keycloak/standalone/deployments"
```

You can then build and run it:

```bash
KEYCLOAK_VERSION=14.0.0
PLUGIN_VERSION=1.0.1

docker build -t custom --build-arg KEYCLOAK_VERSION=$KEYCLOAK_VERSION --build-arg PLUGIN_VERSION=$PLUGIN_VERSION .

docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin custom
```

## server install

Assuming Keycloak is installed at `/opt/keycloak`.

```bash
KEYCLOAK_VERSION=14.0.0
PLUGIN_VERSION=1.0.1

curl -Lo /opt/keycloak/standalone/deployments/${KEYCLOAK_VERSION}-${PLUGIN_VERSION}.jar https://github.com/hoo29/keycloak-client-authz/releases/download/${KEYCLOAK_VERSION}-${PLUGIN_VERSION}/${KEYCLOAK_VERSION}-${PLUGIN_VERSION}.jar

```

# use

TL;DR - Setup a browser authentication flow that uses the `Client Role` authenticator after a standard authenticator provider. Create client roles called `access` on your clients you want to protect and assign members.

---

After installation, Keycloak needs to be configured to use the new authenticator which has a display name of `Client Role` and a provider id of `auth-client-role`.

It needs to be in a sub flow with any authenticator provider (cookie, username /password form, kerberos etc) that is being used. This prevents the user accessing a client they shouldn't be able to after they have authenticated the first time.

An example flow that allows for cookie and username / password looks like:

![screenshot of example flow setup](./docs/flow-setup.jpg 'example flow setup')

It **must** also go below a provider that identifies the user, it cannot go before.

To setup a new Authentication flow:

1. Login to Keycloak as an admin and select the `Authentication` menu item.
1. Create a new flow with `Top Level Flow Type` of `generic`.
1. Do `Add flow` equal to the number of providers you want to use.
1. In each flow, `Add execution` for the desired provider and one for the `Client Role` provider.
1. Set all providers in the sub flows to `REQUIRED`.
1. Set the sub flow to `ALTERNATIVE`.
1. Select the `Bindings` tabs and update the `Browser Flow` value to your new flow.

By default, if the client role does not exist the authenticator will fail open and allow access. This behaviour along with the name of the client role used can be changed in the authenticator config page.

Now we need to create the needed client roles and assign access. This example assigns access directly to the user but you can assign also assign it via groups, realm roles, role mappers etc.

1. Select the `Clients` menu item and select a client.
1. Goto the `Roles` tab and `Add Role`.
1. Provide a name of `access` or what you specified in the authenticator config page.
1. Select the `Users` menu item and find a user that needs access.
1. Goto the `Role Mappings` tab and select the client in the `Client Roles` drop down.
1. Select the `access` role and click `Add selected`.
1. Repeat for all clients you want protecting.

# continuous delivery

Automatic rebuilds happen daily to check for new Keycloak versions.

See [releases](https://github.com/hoo29/keycloak-client-authz/releases) for published versions.

# development

Using JDK 11 and maven 3.8.

Clone the repo:

```bash
git clone https://github.com/hoo29/keycloak-client-authz.git
# or
git clone git@github.com:hoo29/keycloak-client-authz.git
```

Set keycloak version to build against:

```bash
export KEYCLOAK_VERSION=14.0.0
```

Build:

```bash
mvn verify
```

Unlike the main Keycloak codebase, mocks are used to aid in unit testing.

The Wildfly code style is used and stored in [.formatter](./.formatter) folder.

# contributing

I did this as a learning exercise for Keycloak, Java, and GitHub actions. All improvements welcome!

# similar projects

[Cloud Trust's authz module](https://github.com/cloudtrust/keycloak-authorization) also provides client authorisation but makes use of Keycloak's authorisation services instead of client roles. As noted in the project's README it is heavily linked to a specific keycloak version, and has not been updated for several years.
