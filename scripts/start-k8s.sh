#!/bin/bash

minikube start --memory 20000 --cpus=6 --driver=kvm2 --addons ingress
istioctl install -y

sudo ./hosts_remover.sh /etc/hosts
sudo ./hosts_populator.sh /etc/hosts $(minikube ip)
#sudo ./hosts_populator.sh /etc/hosts $(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.clusterIP}')

sleep 20

kubectl apply -f ../k8s/infra
kubectl apply -f ../k8s/istio

#$(minikube tunnel) &

./seed_vault.sh vault.osber.io
./seed_kibana.sh kibana.osber.io create_data_view-k8s-1.json
./seed_kibana.sh kibana.osber.io create_data_view-k8s-2.json

kubectl apply -f ../k8s/services
kubectl apply -f ../k8s/infra/prometheus.yaml

minikube dashboard
