![loans workflow](https://github.com/osber1/loans/actions/workflows/gradle.yml/badge.svg)
[![codecov](https://codecov.io/gh/osber1/loans/branch/improved-application/graph/badge.svg?token=2KOECLUD4M)](https://codecov.io/gh/osber1/loans)

# Loans Application

## Startup

To start the application you need to have installed docker in your machine. Cd into project dir and use command ```docker compose up``` to start PostgreSQL and redis.

## pgAdmin

To log in to [pgAdmin](http://localhost:5050) (admin@admin.com:admin) and add new server connection:

- url: postgres,
- username:root,
- password:root.

## Swagger

[Swagger documentation](http://localhost:8080/swagger-ui/)

### Business requirements

* User can apply for loan by passing amount and term to api.
* Loan application risk analysis is performed. Risk is considered too high if:
    * the attempt to take loan is made after 00:00 with max possible amount.
    * reached max applications (e.g. 3) per day from a single IP.
* Loan is issued if there are no risks associated with the application. If so, client gets response status ok. However, if risk is surrounding the application, client error with
  message.
* Client should be able to extend a loan. Loan term gets extended for one week, interest gets increased by a factor of 1.5.
* The whole history of loans is visible for clients, including loan extensions.
