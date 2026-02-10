FROM eclipse-temurin:21-jdk-alpine

LABEL maintainer="daokhanhhuyen"

WORKDIR /app

VOLUME /tmp

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]