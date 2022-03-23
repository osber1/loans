# Loans Application

## Startup

To start the application you need to have installed docker in your machine. Cd into project dir and use command ```docker compose up``` to start PostgreSQL and redis.

## pgAdmin

To log in to pgAdmin, go to http://localhost:5050 (password is admin) and add new server connection:

- url: postgres,
- username:root,
- password:root.

## Swagger

To see swagger documentation go to http://localhost:8080/swagger-ui/.