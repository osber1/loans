#!/bin/bash    

VERSION="1.0-SNAPSHOT"

docker rm -f loans-back-office
docker rm -f loans-risk-checker
docker rm -f loans-notification-service

docker rmi loans-back-office:$VERSION
docker rmi loans-risk-checker:$VERSION
docker rmi loans-notification-service:$VERSION

docker-compose down
