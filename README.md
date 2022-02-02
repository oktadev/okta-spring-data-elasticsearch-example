# A Quick Guide to Elasticsearch with Spring Data and Spring Boot

This repository contains all the code for the Spring Data Elasticsearch tutorial, illustrating the JHipster support for the Elasticsearch engine in Spring Boot applications.

**Prerequisites**:

- [Java OpenJDK 11](https://jdk.java.net/java-se-ri/11)
- [Okta CLI 0.9.0](https://cli.okta.com)
- [Docker 20.10.7](https://docs.docker.com/engine/install/)

## Getting started

To install this example, first clone this repository:

```bash
git clone https://github.com/oktadev/okta-spring-data-elasticsearch-example.git
```

## Configure Okta authentication

With the Okta CLI, register for a free developer account:

```shell
okta register
```

Provide the required information. Once you complete the registration, create the OIDC client applications.

```shell
cd okta-spring-data-elasticsearch-example
okta apps create jhipster
```

You will be prompted to select the following options:

- Application name: spring-data-elasticsearch
- Redirect URI: http://localhost:8080/login/oauth2/code/oidc,http://localhost:8081/login/oauth2/code/oidc,http://localhost:8761/login/oauth2/code/oidc
- Post Logout Redirect URI: http://localhost:8080,http://localhost:8081,http://localhost:8761

The Okta CLI will create the client application and configure the issuer, client ID and client secret in an `.okta.env` file in the application root folder.

## Run with Docker Compose

In the `blog` and `gateway` root, generate the application container image with the following Maven command:

```shell
./mvnw -DskipTests -ntp -Pprod verify jib:dockerBuild
```

Edit the file `docker-compose/central-server-config/application.yml` and replace the placeholders with the values from `.okta.env`:

```yml
spring:
  security:
    oauth2:
      client:
        provider:
          oidc:
            issuer-uri: https://{yourOktaDomain}/oauth2/default
        registration:
          oidc:
            client-id: {clientId}
            client-secret: {clientSecret}
```

Go to the docker folder and run the services with Docker Compose:

```shell
cd docker-compose
docker compose up
```

Once the services are up, go to http://localhost:8080 and login with your Okta credentials.
