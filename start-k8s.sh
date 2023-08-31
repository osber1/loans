#!/bin/bash

./gradlew clean build

minikube start --memory 20000 --cpus=6 --driver=kvm2 --addons ingress

sudo ./infra_config/hosts_remover.sh /etc/hosts
sudo ./infra_config/hosts_populator.sh /etc/hosts $(minikube ip)

$(minikube dashboard) &

kubectl apply -f k8s/infra
kubectl apply -f k8s/ingress.yaml

./infra_config/vault/seed_vault.sh vault.osber.io
./infra_config/kibana/seed_kibana.sh kibana.osber.io create_data_view-k8s-1.json
./infra_config/kibana/seed_kibana.sh kibana.osber.io create_data_view-k8s-2.json

kubectl apply -f k8s/services