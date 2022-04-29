FROM openjdk:17-jdk-alpine

ARG VERSION
LABEL VERSION=$VERSION
ADD back-office-$VERSION.jar  app.jar

ENTRYPOINT ["java", "-jar", "app.jar" ]
