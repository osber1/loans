FROM openjdk:17-jdk-slim-buster

ARG VERSION
LABEL VERSION=$VERSION
ADD risk-checker-$VERSION.jar  app.jar

ENTRYPOINT ["java", "-jar", "app.jar" ]
