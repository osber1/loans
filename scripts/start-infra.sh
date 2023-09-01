#!/bin/bash

docker-compose -f ../docker/docker-compose.yml up -d

./seed_vault.sh localhost:8200
./seed_kibana.sh localhost:5601 create_data_view.json