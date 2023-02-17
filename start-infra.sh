#!/bin/bash

docker-compose up -d

./infra_config/vault/seed_vault.sh
./infra_config/kibana/seed_kibana.sh