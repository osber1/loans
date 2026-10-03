# Loans Application

## Status

![loans workflow](https://github.com/osber1/loans/actions/workflows/pipeline.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/master/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)
[![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=osber1_loans&metric=sqale_rating)](https://sonarcloud.io/project/overview?id=osber1_loans)
## [Infra Repository](https://github.com/osber1/loans-infra)

## Links

| Docker                                                           | Kubernetes                                                               |
|------------------------------------------------------------------|--------------------------------------------------------------------------|
| [Back-office Actuator](http://localhost:8080/actuator)           | [Back-office Actuator](http://back-office.osber.io/actuator)             |
| [Risk Checker Actuator](http://localhost:8081/actuator)          | [Risk Checker Actuator](http://risk.osber.io/actuator)                   |
| [Notifications Service Actuator](http://localhost:8082/actuator) | [Notifications Service Actuator](http://notifications.osber.io/actuator) |
| [RabbitMQ](http://localhost:15672)                               | [RabbitMQ](http://rabbitmq.osber.io)                                     |
| [Mailhog](http://localhost:8025)                                 | [Mailhog](http://mailhog.osber.io)                                       |
| [pgAdmin](http://localhost:5050)                                 | [pgAdmin](http://pgadmin.osber.io)                                       |
| [Redis Commander](http://localhost:5123)                         | [Redis Commander](http://redis.osber.io)                                 |
| [Vault](http://localhost:8200)                                   | [Vault](http://vault.osber.io)                                           |
| [Prometheus](http://localhost:9090)                              | [Prometheus](http://prometheus.osber.io)                                 |
| [Grafana](http://localhost:3000)                                 | [Grafana](http://grafana.osber.io)                                       |
| [Kibana](http://localhost:5601)                                  | [Kibana](http://kibana.osber.io)                                         |

> This is a demo/portfolio project. The open actuator endpoints and the demo credentials in the configuration
> are intentional so the stack can be explored easily; do not reuse them for anything real. Deployment and
> infrastructure (Docker Compose, Kubernetes, Vault, monitoring) live in
> [osber1/loans-infra](https://github.com/osber1/loans-infra).

## Modules

| Module                 | Description                                                                         |
|------------------------|-------------------------------------------------------------------------------------|
| `back-office`          | Spring Boot application (port 8080): clients, loans, postpones, Swagger UI          |
| `risk-checker`         | Spring Boot application (port 8081): validates loan requests for the back-office    |
| `notification-service` | Spring Boot application (port 8082): consumes RabbitMQ messages and sends emails    |
| `api`                  | Shared library: request/response DTOs, exceptions, validation annotations, utils    |
| `amqp`                 | Shared library: RabbitMQ configuration and message producer                         |
| `acceptance-tests`     | Cucumber end-to-end scenarios that run against a running back-office                |

## Startup

### Backend application

To start the application you need to have Docker and Java 25 installed. Gradle downloads a Java 25 toolchain
automatically if none is installed.

### Intellij IDEA

1) From infra repository scripts folder run `./start-infra.sh` to start all dependencies.
2) In Intellij IDEA run `./gradlew bootRun --parallel`

## Build and test

Always use the Gradle wrapper:

```shell
./gradlew build                      # compile, unit/integration tests, static analysis, boot images
./gradlew build -x test              # build without running tests
./gradlew check                      # tests + Checkstyle, CodeNarc, SpotBugs, JaCoCo coverage verification
./gradlew :back-office:test          # tests of a single module
./gradlew :back-office:bootRun       # run a single application
```

The `back-office` and `notification-service` tests use Testcontainers, so Docker has to be running.
`build` also creates the application images with `bootBuildImage`, which needs Docker as well.

### Acceptance tests

The Cucumber scenarios in `acceptance-tests` call a running back-office, so they are not part of `test`/`check`
(and therefore not part of CI). Start the whole stack (see the infra repository) and run:

```shell
./gradlew :acceptance-tests:acceptanceTest                                      # http://localhost:8080
./gradlew :acceptance-tests:acceptanceTest -Pacceptance.baseUri=http://host:port # any other back-office
```

The base URI can also be set with the `ACCEPTANCE_BASE_URI` environment variable. The HTML report is written to
`acceptance-tests/build/reports/cucumber/cucumber.html`.

### Error Prone

[Error Prone](https://errorprone.info) and [Error Prone Support](https://error-prone.picnic.tech) run on every
Java compilation and report findings of the rule set in `config/error-prone/rules.gradle`; sources are never
modified during a normal build. To let Error Prone apply its suggested fixes (including Refaster rules) to the
sources in place, opt in explicitly and review the resulting diff:

```shell
./gradlew compileJava compileTestJava -PerrorpronePatch --rerun-tasks
```

## CI

`.github/workflows/pipeline.yml` runs `./gradlew check sonar`, uploads coverage to Codecov, scans the repository
with Trivy and, on `master`, logs in to DockerHub and builds the application images. `.github/workflows/ai_pr_review.yml` runs
PR-Agent on pull requests. Required repository secrets:

| Secret               | Used by                       |
|----------------------|-------------------------------|
| `SONAR_TOKEN`        | SonarCloud analysis           |
| `CODECOV_TOKEN`      | Codecov upload                |
| `DOCKERHUB_USERNAME` | DockerHub login (master only) |
| `DOCKERHUB_TOKEN`    | DockerHub login (master only) |
| `OPENAI_KEY`         | PR-Agent review               |

`GITHUB_TOKEN` is provided by GitHub Actions.

## Flow

1) Register user and confirm email in email service.
2) Take loan.
3) You can postpone loan.

## Features

- Code:
    * Lombok
    * MapStruct
    * Swagger
    * Actuator
    * Liquibase
    * Testcontainers
    * JavaMailSender
    * WireMock


- Infra:
    * RabbitMQ
    * Redis
    * PostgreSQL
    * Prometheus
    * Grafana
    * Vault


- DevOps:
    * Docker
    * Kubernetes
    * Istio
    * Argo CD
