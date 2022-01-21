# Tutorial: A Quick Guide to Elasticsearch for Spring Data and Spring Boot

This repository contains all the code for the Spring Data Elasticsearch tutorial, illustrating the JHipster support for the Elasticsearch engine in Spring Boot applications.

**Prerequisites**:
- [Java 11+](https://openjdk.java.net/install/index.html)
- [Okta CLI](https://cli.okta.com)
- [Docker](https://docs.docker.com/engine/install/)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Getting started

To install this example, first clone this repository:

```bash
git clone https://github.com/indiepopart/spring-data-elasticsearch.git
```

## Configure Okta authentication

```shell
cd spring-data-elasticsearch
```

With OktaCLI, register for a free developer account:

```shell
okta register
```
Provide the required information. Once you complete the registration, create the OIDC client applications.

```shell
cd gateway
okta apps create jhipster
```

You will be prompted to select the following options:

- Application name: spring-data-elasticsearch
- Redirect URI: http://localhost:8080/login/oauth2/code/oidc,http://localhost:8081/login/oauth2/code/oidc,http://localhost:8761/login/oauth2/code/oidc
- Post Logout Redirect URI: http://localhost:8080,http://localhost:8081,http://localhost:8761


The OktaCLI will create the client application and configure the issuer, clientId and clientSecret in an `.okta.env` file in the application root folder.

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
