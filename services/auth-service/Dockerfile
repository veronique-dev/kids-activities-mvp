# Build from project root: docker build -f services/auth-service/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY services/auth-service/pom.xml services/auth-service/

RUN mvn -q -pl services/auth-service -am dependency:go-offline

COPY common common
COPY services/auth-service services/auth-service

RUN mvn -q -pl services/auth-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
