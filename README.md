# Loans Application

## Status

![loans workflow](https://github.com/osber1/loans/actions/workflows/gradle.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/master/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)

## Business requirements

* User can apply for loan by passing amount and term to api.
* Loan application risk analysis is performed. Risk is considered too high if:
    * the attempt to take loan is made after 00:00 with max possible amount.
    * reached max applications (e.g. 3) per day from a single IP.
* Loan is issued if there are no risks associated with the application. If so, client gets response status ok. However, if risk is surrounding the application, client error with
  message.
* Client should be able to extend a loan. Loan term gets extended for one week, interest gets increased by a factor of 1.5.
* The whole history of loans is visible for clients, including loan extensions.

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
