# ── ETAPA 1: BUILD ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY backend/gradlew .
COPY backend/gradle gradle
COPY backend/build.gradle .
COPY backend/settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY backend/src src
RUN ./gradlew clean bootJar --no-daemon -x test

# ── ETAPA 2: RUN ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS run

LABEL maintainer="GrupoC - Tienda Ropa"
LABEL description="Backend Spring Boot - Tienda de Ropa"

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Render inyecta PORT automáticamente; EXPOSE es solo documentación
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx256m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]