#!/bin/bash

VERSION="1.0-SNAPSHOT"

minikube delete

sudo ./scripts/hosts_remover.sh /etc/hosts
#kill $(ps aux | grep minikube | awk '{print $2}')

docker rmi osvasldas97/back-office:$VERSION
docker rmi osvasldas97/risk-checker:$VERSION
docker rmi osvasldas97/notification-service:$VERSION