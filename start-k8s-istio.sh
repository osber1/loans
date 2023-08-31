#!/bin/bash

./gradlew clean build

minikube start --memory 20000 --cpus=6 --driver=kvm2
istioctl install -y

sudo ./infra_config/hosts_remover.sh /etc/hosts
sudo ./infra_config/hosts_populator.sh /etc/hosts $(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.clusterIP}')

sleep 20

kubectl apply -f k8s/infra
kubectl apply -f k8s/istio

$(minikube dashboard) &
$(minikube tunnel) &

./infra_config/vault/seed_vault.sh vault.osber.io
./infra_config/kibana/seed_kibana.sh kibana.osber.io create_data_view-k8s-1.json
./infra_config/kibana/seed_kibana.sh kibana.osber.io create_data_view-k8s-2.json

kubectl apply -f k8s/services
kubectl apply -f k8s/infra/prometheus.yaml
