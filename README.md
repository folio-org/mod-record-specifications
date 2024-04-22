# mod-record-specifications
Copyright (C) 2024 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction
Module for managing and validating records against predefined specifications.

## Installing and deployment

### Compiling

Compile with 
```shell
mvn clean install
```

### Running it
Run locally on listening port 8081 (default listening port):

Using Docker to run the local stand-alone instance:

```shell
DB_HOST=localhost DB_PORT=5432 DB_DATABASE=okapi_modules DB_USERNAME=folio_admin DB_PASSWORD=folio_admin \
   java -Dserver.port=8081 -jar target/mod-record-specifications-*.jar
```

### Docker

Build the docker container with:

```shell
docker build -t dev.folio/mod-record-specifications .
```

Test that it runs with:

```shell
docker run -t -i -p 8081:8081 dev.folio/mod-record-specifications
```

### ModuleDescriptor

See the built `target/ModuleDescriptor.json` for the interfaces that this module
requires and provides, the permissions, and the additional module metadata.

## Additional Information

### Issue tracker

See project [MSEARCH](https://folio-org.atlassian.net/browse/MSEARCH)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

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

