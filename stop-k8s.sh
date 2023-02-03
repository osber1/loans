#!/bin/bash    

VERSION="1.0-SNAPSHOT"

kubectl delete -f k8s
kubectl delete -f k8s/services
#kubectl delete -f k8s/istio
minikube delete

docker rmi back-office:$VERSION
docker rmi risk-checker:$VERSION
docker rmi notification-service:$VERSION
docker rmi osvasldas97/back-office:$VERSION
docker rmi osvasldas97/risk-checker:$VERSION
docker rmi osvasldas97/notification-service:$VERSION