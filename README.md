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

## Startup

### Backend application

To start the application you need to have installed docker in your machine. Cd into project dir and use command ```docker compose up``` to start PostgreSQL and redis.

### RabbitMQ

1) Add new `Queue` with name `notification.queue`
2) Add new `Exchange` with name `internal.exchange`
3) Add new `Binding` in `internal.exchange` exchange:
    * `To queue`: `notification.queue`
    * `routingKey`: `internal.notification.routing-key`

## [pgAdmin](http://localhost:5050)

* Username: admin@admin.com
* Password: admin

### Add new server connection

- url: postgres,
- username: root,
- password: root.

## Flow

1) Register user and confirm email in [email service](http://localhost:1080).

## [Swagger documentation](http://localhost:8080/swagger-ui.html)

## [Actuator](http://localhost:8080/actuator)

## [RabbitMQ](http://localhost:15672)

* Username: guest
* Password: guest

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