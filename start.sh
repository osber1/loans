#!/bin/bash

./gradlew clean build

docker-compose -f docker/docker-compose.yml up -d

./docker/infra_config/vault/seed_vault.sh localhost:8200
./docker/infra_config/kibana/seed_kibana.sh localhost:5601 create_data_view.json

docker-compose -f docker/docker-compose-applications.yml up -d