#!/bin/bash

./gradlew clean build

docker-compose up -d

./infra_config/vault/seed_vault.sh localhost 8200

docker-compose -f docker-compose-applications.yml up -d

./infra_config/kibana/seed_kibana.sh