#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Usage: $0 <filename> <minikube_ip>"
    exit 1
fi

filename="$1"
minikube_ip="$2"

services=(
"back-office.osber.io"
"risk.osber.io"
"notifications.osber.io"
"vault.osber.io"
"redis.osber.io"
"rabbitmq.osber.io"
"prometheus.osber.io"
"pgadmin.osber.io"
"mailhog.osber.io"
"kibana.osber.io"
"grafana.osber.io"
"argocd.osber.io"
)

for service in "${services[@]}"; do
    echo "$minikube_ip $service" >> "$filename"
done
