#!/bin/bash    

VERSION="1.0-SNAPSHOT"

kubectl delete -f k8s/postgresql/postgres.yaml
kubectl delete -f k8s/postgresql/pg-admin.yaml
#minikube delete

docker rmi back-office:$VERSION
docker rmi risk-checker:$VERSION
docker rmi notification-service:$VERSION