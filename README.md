# keycloak-client-authz

Keycloak plugin for client authorisation. Allows using membership of client roles to enforce login authorisation during Keycloak authentication. Keycloak's built in authorisation services only provide for evaluation of policies but delegates enforcement to the clients themselves. For COTS software and SPA web apps this isn't always feasible.

# installation

The are several options to install this plugin depending on how you are running Keycloak. Below are a couple of docker based ideas. For more information, checkout the official [Keycloak docs](https://www.keycloak.org/docs/latest/server_development/#registering-provider-implementations).

## custom docker image

```dockerfile
FROM quay.io/keycloak/keycloak:latest

USER root
COPY ["something.jar", "/opt/jboss/keycloak/standalone/deployments"]
RUN chmod go+rx /opt/jboss/keycloak/standalone/deployments
USER 1000
# use ENTRYPOINT from official image
```

## docker volume mount

```bash
curl -Lo https://github.com/hoo29/keycloak-client-authz/releases/download/v2.0.0/sonar-auth-oidc-plugin-2.0.0.jar ~/deployments

docker run -dit \
    -p 8080:8080 \
    -e KEYCLOAK_USER=admin \
    -e KEYCLOAK_PASSWORD=admin \
    --mount src=~/deployments,target=/opt/jboss/keycloak/standalone/deployments \
    quay.io/keycloak/keycloak:14.0.0
```

# use

# versioning

The plugin releases are formed of two versions `x.x.x-y.y.y` where `x.x.x` is they Keycloak version this plugin was built against and `y.y.y` is the semantic version of the plugin. Details of the plugin version can be found in the [CHANGELOG](./CHANGELOG.md).

# continuos delivery

Automatic rebuilds happen daily to check for new Keycloak versions. Rebuilding is used as a means to check the Keycloak SPI has not changed. It does not change the plugin jar itself as no Keycloak components are bundled. All of the dependencies have a `scope` of `provided`.

Newer versions of this plugin will only be built against the latest Keycloak but will likely work with any version as the Authenticator SPI is fairly stable, although private.

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

# contributing

I did this as a learning exercise for Keycloak and GitHub actions. All improvements welcome!

# similar projects

[Cloud Trust's authz module](https://github.com/cloudtrust/keycloak-authorization) also provides client authorisation but makes use of Keycloak's authorisation services instead of client roles. As noted in the project's README it is heavily linked to a specific keycloak version, and has not been updated for several years.
