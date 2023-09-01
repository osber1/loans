#!/bin/bash

# Running first time
#helm repo add hashicorp https://helm.releases.hashicorp.com
#helm repo add argo-cd https://argoproj.github.io/argo-helm

# Init
#minikube start --memory 20000 --cpus=6 --driver=kvm2 --addons ingress
#sudo ../scripts/hosts_remover.sh /etc/hosts
#sudo ../scripts/hosts_populator.sh /etc/hosts $(minikube ip)
#kubectl apply -f ../k8s/infra/_namespace-loans.yaml

# Install Helm services
#helm upgrade --install vault hashicorp/vault --values vault-values.yml -n loans
#helm upgrade --install redis oci://registry-1.docker.io/bitnamicharts/redis --values redis-values.yml -n loans
#helm upgrade --install redis-commander ./redis-commander --values redis-commander-values.yml -n loans

# Seed
#../scripts/seed_vault.sh vault.osber.io

# not done yet
#../scripts/seed_kibana.sh kibana.osber.io create_data_view-k8s-1.json
#../scripts/seed_kibana.sh kibana.osber.io create_data_view-k8s-2.json

helm list -n loans

# Remove Helm services
#helm uninstall vault -n loans
#helm uninstall redis -n loans
#helm uninstall redis-commander -n loans
