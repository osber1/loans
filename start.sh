#!/bin/bash    

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
#./gradlew clean build

docker-compose up -d

docker build -f back-office/Dockerfile -t loans-back-office:$VERSION --build-arg VERSION=$VERSION back-office/build/libs
docker build -f fraud-checker/Dockerfile -t loans-fraud-checker:$VERSION --build-arg VERSION=$VERSION fraud-checker/build/libs
docker build -f notification-service/Dockerfile -t loans-notification-service:$VERSION --build-arg VERSION=$VERSION notification-service/build/libs

docker run --name loans-back-office -d --network="host" loans-back-office:$VERSION
docker run --name loans-fraud-checker -d --network="host" loans-fraud-checker:$VERSION
docker run --name loans-notification-service -d --network="host" loans-notification-service:$VERSION