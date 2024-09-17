# mod-record-specifications
Copyright (C) 2024 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.folio%3Amod-record-specifications&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=org.folio%3Amod-record-specifications)

## Introduction

mod-record-specifications is a module for managing and validating records against predefined specifications.

## Installation and Deployment

### Compiling

To compile mod-record-specifications, use the command:

```shell
mvn clean install
```

### Tenant attributes

It is possible to define specific tenant parameters during module's initialization for particular tenant.

| Tenant parameter    | Default value | Description                                                 |
|:--------------------|:-------------:|:------------------------------------------------------------|
| syncSpecifications  |     true      | Sync Specifications as module is enabled for the new tenant |


### Environment Variables

Below are the environment variables used by this module:

| Variable Name                                       | Default Value | Description                                                                                                                                                |
|-----------------------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DB_HOST                                             | localhost     | Postgres hostname                                                                                                                                          |
| DB_PORT                                             | 5432          | Postgres port                                                                                                                                              |
| DB_USERNAME                                         | folio_admin   | Postgres username                                                                                                                                          |
| DB_PASSWORD                                         | folio_admin   | Postgres username password                                                                                                                                 |
| DB_DATABASE                                         | okapi_modules | Postgres database name                                                                                                                                     |
| DB_MAXPOOLSIZE                                      | 10            | Max pool size allowed, including both idle and in-use connections                                                                                          |
| DB_MINIMUM_IDLE                                     | 10            | Min number of idle connections that HikariCP tries to maintain in the pool                                                                                 |
| DB_CONNECTION_TIMEOUT                               | 30000         | Max number of milliseconds that a client will wait for a connection from the pool                                                                          |
| DB_IDLE_TIMEOUT                                     | 600000        | Max amount of time that a connection is allowed to sit idle in the pool, applicable when `DB_MINIMUM_IDLE` < `DB_MAXPOOLSIZE`                              |
| DB_KEEPALIVE_TIME                                   | 0             | Frequency of connection keep alive attempts by HikariCP to prevent time-out by database/network. 0 means disabled                                          |
| DB_MAX_LIFETIME                                     | 1800000       | Max lifetime of a connection in the pool                                                                                                                   |
| DB_VALIDATION_TIMEOUT                               | 5000          | Max amount of time a connection is tested for aliveness. Must be less than `DB_CONNECTION_TIMEOUT`                                                         |
| DB_INITIALIZATION_FAIL_TIMEOUT                      | 30000         | Defines whether the pool will 'fail fast' if it can't be seeded with an initial connection                                                                 |
| DB_LEAK_DETECTION_THRESHOLD                         | 30000         | Time a connection can be out of the pool before a message is logged for a possible connection leak. 0 means disabled                                       |
| KAFKA_HOST                                          | kafka         | Kafka broker hostname                                                                                                                                      |
| KAFKA_PORT                                          | 9092          | Kafka broker port                                                                                                                                          |
| KAFKA_SECURITY_PROTOCOL                             | PLAINTEXT     | Kafka security protocol used to communicate with brokers (SSL or PLAINTEXT)                                                                                |
| KAFKA_SSL_KEYSTORE_LOCATION                         | -             | The location of the Kafka key store file. This is optional for client and can be used for two-way authentication for client.                               |
| KAFKA_SSL_KEYSTORE_PASSWORD                         | -             | The store password for the Kafka key store file. This is optional for client and only needed if 'ssl.keystore.location' is configured.                     |
| KAFKA_SSL_TRUSTSTORE_LOCATION                       | -             | The location of the Kafka trust store file.                                                                                                                |
| KAFKA_SSL_TRUSTSTORE_PASSWORD                       | -             | The password for the Kafka trust store file. If a password is not set, trust store file configured will still be used, but integrity checking is disabled. |
| KAFKA_SPECIFICATION_UPDATE_TOPIC_PARTITIONS         | 1             | Amount of partitions for `specification-storage.specification.updated` topic.                                                                              |
| KAFKA_SPECIFICATION_UPDATE_TOPIC_REPLICATION_FACTOR | -             | Replication factor for `specification-storage.specification.updated` topic.                                                                                |

Change these variables as per your requirements.

### Running The Module

#### Locally

Run the module locally on the default listening port (8081) with the prescribed command:

```shell
DB_HOST=localhost DB_PORT=5432 DB_DATABASE=okapi_modules DB_USERNAME=folio_admin DB_PASSWORD=folio_admin \
java -Dserver.port=8081 -jar mod-record-specifications-server/target/mod-record-specifications-fat.jar
```

#### Using Docker

To run the module in a Docker container, first build the Docker image:

```shell
docker build -t dev.folio/mod-record-specifications .
```

Prepare infrastructure needed for the module: PostgreSQL database.
Alternatively, you can use Docker Compose to manage the application's infrastructure.
```shell
docker compose up
```

Then run the container:

```shell
docker run -t -i -p 8081:8081 dev.folio/mod-record-specifications
```

### Module Descriptor

See the built target/ModuleDescriptor.json file for the interfaces, permissions, and additional modules required and provided by this one.

## Additional Information

### Development documentation

Additional development documentation exists in the [development.md](docs%2Fdevelopment.md)

### Issue Tracker

If you find any issues or bugs, please report at project [MRSPECS](https://folio-org.atlassian.net/browse/MRSPECS) at the FOLIO issue tracker.

### API Documentation

This module's [API documentation](https://dev.folio.org/reference/api/#mod-record-specifications).

### Code analysis

[SonarQube analysis](https://sonarcloud.io/dashboard?id=org.folio%3Amod-record-specifications).

### Download and configuration

The built artifacts for this module are available.
See [configuration](https://dev.folio.org/download/artifacts) for repository access,
and the [Docker image](https://hub.docker.com/r/folioorg/mod-record-specifications/)

### Code of Conduct

[FOLIO Code of Conduct](https://folio-org.atlassian.net/wiki/spaces/COMMUNITY/pages/4231255/FOLIO+Code+of+Conduct)

