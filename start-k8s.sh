#!/bin/bash

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build -x test --parallel
docker tag risk-checker:$VERSION osvasldas97/risk-checker:$VERSION
docker tag back-office:$VERSION osvasldas97/back-office:$VERSION
docker tag notification-service:$VERSION osvasldas97/notification-service:$VERSION
docker push osvasldas97/risk-checker:$VERSION
docker push osvasldas97/back-office:$VERSION
docker push osvasldas97/notification-service:$VERSION

# Uncomment only running for the first time
minikube start --memory 16384 --cpus=4 --driver=kvm2
minikube addons enable ingress
#istioctl install -y

sleep 5

kubectl apply -f k8s/infra
#kubectl apply -f k8s/istio

# Seed Vault
MINIKUBE_IP=`minikube ip`
VAULT_PORT=`kubectl get service vault -n loans -o jsonpath='{.spec.ports[0].nodePort}'`
./infra_config/vault/seed_vault.sh $MINIKUBE_IP $VAULT_PORT
kubectl apply -f k8s/services

minikube service list