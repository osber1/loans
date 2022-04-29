#!/bin/bash    

VERSION="1.0-SNAPSHOT"

# Need to have JAVA 17 installed.
./gradlew clean build --parallel

./start-infra.sh

docker build -f back-office/Dockerfile -t loans-back-office:$VERSION --build-arg VERSION=$VERSION back-office/build/libs
docker build -f risk-checker/Dockerfile -t loans-risk-checker:$VERSION --build-arg VERSION=$VERSION risk-checker/build/libs
docker build -f notification-service/Dockerfile -t loans-notification-service:$VERSION --build-arg VERSION=$VERSION notification-service/build/libs

docker run --name loans-back-office -d --network="host" loans-back-office:$VERSION
docker run --name loans-risk-checker -d --network="host" loans-risk-checker:$VERSION
docker run --name loans-notification-service -d --network="host" loans-notification-service:$VERSION
