# Tutorial: A Quick Guide to Elasticsearch for Spring Data and Spring Boot

This repository contains all the code for the Spring Data Elasticsearch tutorial, illustrating the JHipster support for the Elasticsearch engine in Spring Boot applications.

**Prerequisites**:
- [HTTPie](https://httpie.io/)
- [JHipster 7](https://www.jhipster.tech/installation/)
- [Java 14+](https://openjdk.java.net/install/index.html)
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

- Application name: gateway
- Type of Application: JHipster
- Redirect URI: Default
- Post Logout Redirect URI: Default

Make sure to setup http://localhost:8081/login/oauth2/code/oidc with port 8081 as Redirect URI for blog microservice.

```shell
cd blog
okta apps create jhipster
```

The OktaCLI will create the client application and configure the issuer, clientId and clientSecret in an `.okta.env` file in the application root folder.

## Run with Docker Compose

In the `blog` and `gateway` root, generate the application container image with the following Maven command:

```shell
./mvnw spring-boot:build-image
```

Add the file `docker-compose/central-server-config/blog-prod.yml` with the following content:

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
            client-id: {blogClientId}
            client-secret: {blogClientSecret}
```

Replace the placeholders with the values from `.okta.env`. Add also the file `docker-compose/central-server-config/gateway-prod.yml` with the following content, also replacing the placeholders:

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
            client-id: {gatewayClientId}
            client-secret: {gatewayClientSecret}
```


Go to the docker folder and run the services with Docker Compose:

```shell
cd docker-compose
docker compose up
```

Once the services are up, go to http://localhost:8080 and login with your Okta credentials.
