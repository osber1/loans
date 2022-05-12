# Loans Application

## Status

![loans workflow](https://github.com/osber1/loans/actions/workflows/gradle.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/master/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)

## Links

- [Swagger documentation](http://localhost:8080/swagger-ui.html)
- [Actuator](http://localhost:8080/actuator)
- [RabbitMQ](http://localhost:15672)
- [Email service](http://localhost:1080)
- [pgAdmin](http://localhost:5050)
- [Redis Commander](http://localhost:5123)
- [Vault](http://localhost:8200)

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

## Vault

* Method: Token
* Token: super-secret-token

### Add new server connection

- url: postgres,
- username: root,
- password: root.

## RabbitMQ

* Username: guest
* Password: guest

## Flow

1) Register user and confirm email in email service.
2) Take loan.
3) You can postpone loan.

## Features

* Swagger
* Lombok
* MapStruct
* Actuator
* RabbitMQ
* Redis
* PostgreSQL
* Liquibase
* Testcontainers
* JavaMailSender
* WireMock
