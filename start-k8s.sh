#!/bin/bash    

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build --parallel

#minikube start --vm-driver kvm2
kubectl apply -f k8s/postgresql/postgres.yaml
kubectl apply -f k8s/postgresql/pg-admin.yaml

sleep 1

# Seed Vault
curl -H "X-Vault-Token: super-secret-token" -H "Content-Type: application/json" -X POST -d @vault/application.json http://127.0.0.1:8200/v1/secret/data/application