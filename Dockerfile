# ── ETAPA 1: BUILD ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Instalar Node.js + npm
RUN apk add --no-cache nodejs npm

# ── Copiar backend ────────────────────────────────────────────────────────────
COPY backend/gradlew ./gradlew
COPY backend/gradle ./gradle
COPY backend/build.gradle ./build.gradle
COPY backend/settings.gradle ./settings.gradle
COPY backend/src ./src

# ── Copiar frontend (tu carpeta se llama fronted) ────────────────────────────
COPY fronted ./fronted

# Permisos gradlew
RUN chmod +x gradlew

# Build completo (frontend + backend)
RUN ./gradlew clean bootJar --no-daemon -x test

# ── ETAPA 2: RUN ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS run

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx256m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]