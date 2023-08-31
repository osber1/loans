#!/bin/bash

VERSION="1.0-SNAPSHOT"

kubectl delete -f k8s/infra
kubectl delete -f k8s/services
kubectl delete -f k8s/istio
minikube delete

sudo ./infra_config/hosts_remover.sh /etc/hosts
#kill $(ps aux | grep minikube | awk '{print $2}')

docker rmi osvasldas97/back-office:$VERSION
docker rmi osvasldas97/risk-checker:$VERSION
docker rmi osvasldas97/notification-service:$VERSION