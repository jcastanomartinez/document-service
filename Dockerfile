FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -e -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache chromium nss freetype harfbuzz ca-certificates fontconfig

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
