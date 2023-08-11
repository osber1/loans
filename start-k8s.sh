#!/bin/bash

./gradlew clean build

minikube start --memory 20000 --cpus=6 --driver=kvm2
minikube addons enable ingress
istioctl install -y

sleep 20

kubectl apply -f k8s/infra
kubectl apply -f k8s/istio

# Seed Vault
MINIKUBE_IP=`minikube ip`
VAULT_PORT=`kubectl get service vault -n loans -o jsonpath='{.spec.ports[0].nodePort}'`
./infra_config/vault/seed_vault.sh $MINIKUBE_IP $VAULT_PORT

KIBANA_PORT=`kubectl get service kibana -n loans -o jsonpath='{.spec.ports[0].nodePort}'`
./infra_config/kibana/seed_kibana.sh $MINIKUBE_IP $KIBANA_PORT create_data_view-k8s.json
kubectl apply -f k8s/services

minikube service list
