# Loans Application

## Status

![loans workflow](https://github.com/osber1/loans/actions/workflows/docker-push.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/master/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)

## Links

- [Swagger documentation](http://localhost:8080/swagger-ui.html)
- [Actuator](http://localhost:8080/actuator)
- [RabbitMQ](http://localhost:15672)
- [Email service](http://localhost:8025)
- [pgAdmin](http://localhost:5050)
- [Redis Commander](http://localhost:5123)
- [Vault](http://localhost:8200)
- [Prometheus](http://localhost:9090)
- [Grafana](http://localhost:3000)
- [Kibana](http://localhost:5601)

## Startup

### Backend application

To start the application you need to have Docker ant Java 17 installed.

### Intellij IDEA

1) Use command `./start-infra.sh` to start all dependencies.
2) In Intellij IDEA run `gradle clean bootRun --parallel`

### Command Line

Run `./start.sh` to start everything.

## pgAdmin

* Username: admin@admin.com
* Password: admin

### Add new server connection

- url: postgres,
- username: root,
- password: root.

## Vault

* Method: Token
* Token: super-secret-token

## RabbitMQ

* Username: guest
* Password: guest

## Grafana

* Username: admin
* Password: admin

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
