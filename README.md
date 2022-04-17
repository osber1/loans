# Loans Application

## Status

![loans workflow](https://github.com/osber1/loans/actions/workflows/gradle.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/master/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)

## Business requirements

* Fully working API.
* Risk rules:
  * loan risk too high when taken within forbidden time with max amount.
  * exceeded max loans per day limit.
* Loan extension - term gets extended and interest gets increased.

## Links

- [Swagger documentation](http://localhost:8080/swagger-ui.html)
- [Actuator](http://localhost:8080/actuator)
- [RabbitMQ](http://localhost:15672)
- [Email service](http://localhost:1080)
- [pgAdmin](http://localhost:5050)

## Startup

### Backend application

To start the application you need to have installed docker in your machine:

1) Use command `docker compose up` to start all dependencies.
2) In Intellij IDEA run `gradle clean bootRun`
3) Run backoffice and notification services.s

### RabbitMQ

1) Add new `Queue` with name `notification.queue`
2) Add new `Exchange` with name `internal.exchange`
3) Add new `Binding` in `internal.exchange` exchange:
    * `To queue`: `notification.queue`
    * `routingKey`: `internal.notification.routing-key`

## pgAdmin

* Username: admin@admin.com
* Password: admin

### Add new server connection

- url: postgres,
- username: root,
- password: root.

## RabbitMQ

* Username: guest
* Password: guest

## Flow

1) Register user and confirm email in email service.

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
