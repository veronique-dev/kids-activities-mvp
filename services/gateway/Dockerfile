# Build from project root: docker build -f services/gateway/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY services/gateway/pom.xml services/gateway/

RUN mvn -q -pl services/gateway -am dependency:go-offline

COPY common common
COPY services/gateway services/gateway

RUN mvn -q -pl services/gateway -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
