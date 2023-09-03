# Loans Application

## Status

![loans workflow](https://github.com/osber1/loans/actions/workflows/docker-push.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/master/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)

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

## Startup

### Backend application

To start the application you need to have Docker ant Java 17 installed.

### Intellij IDEA

1) From infra repository scripts folder run `./start-infra.sh` to start all dependencies.
2) In Intellij IDEA run `gradle clean bootRun --parallel`

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
