FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

VOLUME /tmp

# Copy file jar
COPY target/*.jar app.jar

EXPOSE 8082

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]