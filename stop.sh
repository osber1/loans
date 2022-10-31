#!/bin/bash    

VERSION="1.0-SNAPSHOT"

docker-compose -f docker-compose.yml -f docker-compose-applications.yml down

docker rmi back-office:$VERSION
docker rmi risk-checker:$VERSION
docker rmi notification-service:$VERSION