#!/bin/bash

./gradlew clean build

docker-compose up -d

./infra_config/vault/seed_vault.sh localhost:8200
./infra_config/kibana/seed_kibana.sh localhost:5601 create_data_view.json

docker-compose -f docker-compose-applications.yml up -d