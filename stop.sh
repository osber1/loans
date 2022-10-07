#!/bin/bash    

VERSION="1.0-SNAPSHOT"

docker-compose -f docker-compose-full.yml down

docker rmi back-office:$VERSION
docker rmi risk-checker:$VERSION
docker rmi notification-service:$VERSION