# Build from project root: docker build -f services/notification-service/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY services/notification-service/pom.xml services/notification-service/

RUN mvn -q -pl services/notification-service -am dependency:go-offline

COPY common common
COPY services/notification-service services/notification-service

RUN mvn -q -pl services/notification-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
