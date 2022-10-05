#!/bin/bash    

VERSION="1.0-SNAPSHOT"

docker-compose -f docker-compose-full.yml down

docker rmi loans-back-office:$VERSION
docker rmi loans-risk-checker:$VERSION
docker rmi loans-notification-service:$VERSION