#!/bin/bash

./gradlew clean build

minikube start --memory 20000 --cpus=6 --driver=kvm2 --addons ingress
istioctl install -y

sudo ./scripts/hosts_remover.sh /etc/hosts
sudo ./scripts/hosts_populator.sh /etc/hosts $(minikube ip)
#sudo ./scripts/hosts_populator.sh /etc/hosts $(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.clusterIP}')

sleep 20

kubectl apply -f k8s/infra
kubectl apply -f k8s/istio

#$(minikube tunnel) &

./docker/infra_config/vault/seed_vault.sh vault.osber.io
./docker/infra_config/kibana/seed_kibana.sh kibana.osber.io create_data_view-k8s-1.json
./docker/infra_config/kibana/seed_kibana.sh kibana.osber.io create_data_view-k8s-2.json

kubectl apply -f k8s/services
kubectl apply -f k8s/infra/prometheus.yaml

minikube dashboard
