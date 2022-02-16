# A Quick Guide to Elasticsearch with Spring Data and Spring Boot

This repository contains all the code for a Spring Data Elasticsearch tutorial, illustrating JHipster's support for the Elasticsearch engine in Spring Boot applications. Please read [A Quick Guide to Elasticsearch with Spring Data and Spring Boot][blog] to see how it was created. 

**Prerequisites**:

- [Java OpenJDK 11](https://jdk.java.net/java-se-ri/11)
- [Okta CLI 0.9.0](https://cli.okta.com)
- [Docker 20.10.7](https://docs.docker.com/engine/install/)

> [Okta](https://developer.okta.com/) has Authentication and User Management APIs that reduce development time with instant-on, scalable user infrastructure. Okta's intuitive API and expert support make it easy for developers to authenticate, manage and secure users and roles in any application.

* [Getting Started](#getting-started)
* [Links](#links)
* [Help](#help)
* [License](#license)

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

## Links

This example uses the following open source libraries from Okta:

* [Okta CLI](https://github.com/okta/okta-cli)

## Help

Please post any questions as comments on [this example's blog post][blog], or use our [Okta Developer Forums](https://devforum.okta.com/).

## License

Apache 2.0, see [LICENSE](LICENSE).

[blog]: https://developer.okta.com/blog/2022/02/16/spring-data-elasticsearch
