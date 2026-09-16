# syntax=docker/dockerfile:1

# ---------------------------------------------------------------
# Stage 1 — build del jar
# ---------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Cacheo de dependencias: primero el pom, luego el codigo
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------
# Stage 2 — runtime con Chromium para Playwright
# ---------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Ruta compartida del navegador (fuera de /root para poder correr sin root)
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

WORKDIR /app

COPY --from=build /build/target/document-service-0.0.1-SNAPSHOT.jar app.jar

# Instala Chromium + sus librerias de sistema usando el CLI de Playwright
# que viene dentro del propio fat jar de Spring Boot.
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        ca-certificates \
        fonts-liberation \
        fonts-noto-color-emoji \
 && java -cp app.jar \
        -Dloader.main=com.microsoft.playwright.CLI \
        org.springframework.boot.loader.launch.PropertiesLauncher \
        install --with-deps chromium \
 && chmod -R a+rX /ms-playwright \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# Usuario sin privilegios
RUN useradd --create-home --shell /bin/bash appuser
USER appuser

EXPOSE 8083

ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=70", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]