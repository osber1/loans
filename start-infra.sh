#!/bin/bash

docker-compose up -d

sleep 1

# Seed Vault
curl -H "X-Vault-Token: super-secret-token" -H "Content-Type: application/json" -X POST -d @vault/application.json http://127.0.0.1:8200/v1/secret/data/application