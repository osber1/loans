#!/bin/bash    

VERSION="1.0-SNAPSHOT"

docker-compose -f docker-compose.yml -f docker-compose-applications.yml down

docker rmi osvasldas97/back-office:$VERSION
docker rmi osvasldas97/risk-checker:$VERSION
docker rmi osvasldas97/notification-service:$VERSION