# ─────────────────────────────────────────────────────────────────────────────
# Dockerfile – Multi-etapa: Build → Run
#
# Laboratorio 2 - Parte 2, Paso 1d:
#   Etapa 1 (build):  Compila el proyecto con Gradle y genera el .jar
#   Etapa 2 (run):    Imagen ligera que solo ejecuta el .jar final
#
# Esto reduce el tamaño final de la imagen eliminando el JDK y las
# herramientas de build (solo queda el JRE + el .jar).
# ─────────────────────────────────────────────────────────────────────────────

# ── ETAPA 1: BUILD ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos de configuración de Gradle primero
# (aprovecha la caché de Docker si no cambiaron)
COPY backend/gradlew .
COPY backend/gradle gradle
COPY backend/build.gradle .
COPY backend/settings.gradle .

# Da permisos al wrapper
RUN chmod +x gradlew

# Descarga las dependencias antes de copiar el código fuente
# (capa cacheada — solo se re-ejecuta si build.gradle cambia)
RUN ./gradlew dependencies --no-daemon || true

# Copia el código fuente
COPY backend/src src

# Compila y genera el .jar (sin ejecutar tests — los tests corren en CI)
RUN ./gradlew clean bootJar --no-daemon -x test

# ── ETAPA 2: RUN ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS run

# Metadatos de la imagen
LABEL maintainer="GrupoC - Tienda Ropa"
LABEL description="Backend Spring Boot - Tienda de Ropa"

WORKDIR /app

# Copia únicamente el .jar generado en la etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto que expone la aplicación (debe coincidir con server.port en application.yml)
EXPOSE 8080

# Variables de entorno que Render inyecta (sobreescriben application.yml)
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de arranque con opciones de memoria adecuadas para Render (free tier)
ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx256m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
