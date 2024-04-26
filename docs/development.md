# Development documentation

## Spectral

Spectral is an extensible platform for creating linting rules to enforce your style guide across your API descriptions.

Use it for linting API definition against [rules file](..%2F.spectral.yaml).

You can install it locally via npm:
```shell
npm install -g @stoplight/spectral
```

You can run Spectral against your API definition from repository root like so:

```shell
spectral lint api/mod-record-specifications.yaml -F hint
```

Check out the [Spectral documentation](https://stoplight.io/p/docs/gh/stoplightio/spectral/docs/getting-started/rulesets.md) on how to write and organize rules.

## Spring Boot Dev Tools

Spring Boot DevTools is a set of tools and features providing developers with a better development experience.

It does constitute several features including:
* Automatic restart: Anytime files on the classpath change, the application is restarted. This saves the time often spent redeploying changes.
* Live reload: With the LiveReload browser extension, you can automatically refresh your browser anytime files change.

To use just start application as usual, do changes and code, build project and changes will be reflected without restarting.

See the [official reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-devtools) for more information about Spring Boot DevTools.


## Docker Compose Support in Spring Boot

Docker Compose is a service that you can use to define and manage multiple container applications. 
Spring Boot provides support for Docker Compose through the `spring-boot-docker-compose` module. 
When included as a dependency, Spring Boot will automatically start the services defined in your `compose.yml`, 
create service connection beans for each supported container and stop the services when the application is shutdown.

To use Docker Compose with Spring Boot, you need to do the following:

1. Ensure the docker and docker compose command-line applications are installed on your machine.
2. Spring Boot will perform various tasks such as looking for a `compose.yml` and calling `docker compose up` and `docker compose stop` during its lifecycle. You can customize this process with properties such as `spring.docker.compose.lifecycle-management`, `spring.docker.compose.start.command`, and `spring.docker.compose.stop.command`.
3. If your setup uses a custom image name, you can use a label `org.springframework.boot.service-connection: custom` in your `compose.yml` to ensure Spring Boot can find it.
4. If you want to ignore a specific container in your compose file, tag it with the label `org.springframework.boot.ignore: true`.
5. Finally, remember to run your application, and Spring Boot will take care of running the compose file, and creating service connections for supported containers. The setup should work automatically, but remember you have many options to tweak the behavior. Enjoy your streamlined Spring Boot and Docker Compose development process!

More details can be found in the official [Spring Boot Docker Compose](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.docker-compose) documentation.