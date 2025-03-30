# Remitly2025 - SWIFT codes API

The project is a Spring Boot REST API built with Java 17 and MongoDB.
It utilizes Spring Data MongoDB, Spring Web, and various other libraries, including JSON processing and validation.

## Building and Running

### Local Build

To run the project locally, ensure you have Java 17 installed. You can use the Gradle wrapper provided in the
repository. Additionally, MongoDB must be running locally, and the connection parameters should be configured in the
`application.properties` file located in the `/rest-api` project folder. Once configured, you can start the application
with the following commands:

```shell
cd rest-api

./gradlew bootRun
```

### Docker Build

To run a Dockerized version of the application, you first need to generate a JAR file and build a Docker image that
uses it. Execute the following commands:

```shell
cd rest-api

./gradlew clean bootJar
```

Next, navigate back to the root folder and build the Docker image using Docker Compose:

```shell
cd ..

docker compose build
```

> **_Note:_** If you are using an older version of Docker Compose, you may need to use `docker-compose` instead.

Finally, start the application using Docker Compose:

```shell
docker compose up
```

## Running Tests

All tests are implemented using JUnit and can be executed with Gradle from the `/rest-api` folder. To run integration
tests with MongoDB TestContainer, ensure that the Docker socket is available.

```shell
cd rest-api

./gradlew test
```

