#!/bin/bash

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build --parallel

docker-compose -f docker-compose.yml -f docker-compose-applications.yml up -d

./infra_config/vault/seed_vault.sh localhost 8200
./infra_config/kibana/seed_kibana.sh