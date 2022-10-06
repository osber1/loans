#!/bin/bash    

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build --parallel

docker build -f back-office/Dockerfile -t loans-back-office:$VERSION --build-arg VERSION=$VERSION back-office/build/libs
docker build -f risk-checker/Dockerfile -t loans-risk-checker:$VERSION --build-arg VERSION=$VERSION risk-checker/build/libs
docker build -f notification-service/Dockerfile -t loans-notification-service:$VERSION --build-arg VERSION=$VERSION notification-service/build/libs

docker-compose -f docker-compose-full.yml up -d

sleep 1

# Seed Vault
curl -H "X-Vault-Token: super-secret-token" -H "Content-Type: application/json" -X POST -d @vault/application.json http://127.0.0.1:8200/v1/secret/data/application