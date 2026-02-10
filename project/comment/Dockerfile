FROM eclipse-temurin:21-jdk-alpine

LABEL maintainer="daokhanhhuyen"

WORKDIR /app

VOLUME /tmp

COPY target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]