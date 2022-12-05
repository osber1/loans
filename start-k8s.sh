#!/bin/bash    

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build --parallel
docker tag risk-checker:$VERSION osvasldas97/risk-checker:$VERSION
docker tag back-office:$VERSION osvasldas97/back-office:$VERSION
docker tag notification-service:$VERSION osvasldas97/notification-service:$VERSION
docker push osvasldas97/risk-checker:$VERSION
docker push osvasldas97/back-office:$VERSION
docker push osvasldas97/notification-service:$VERSION

# Uncomment only running for the first time
#minikube config set memory 16384
#minikube config set cpus 4
#minikube config set vm-driver kvm2
#minikube start
#istioctl install -y

kubectl apply -f k8s
#kubectl apply -f k8s/istio

#sleep 1

# Seed Vault
#curl -H "X-Vault-Token: super-secret-token" -H "Content-Type: application/json" -X POST -d @vault/application.json http://127.0.0.1:8200/v1/secret/data/application

minikube service list