# ============================================================
# Dockerfile para Backend Spring Boot (Java 21)
# Multi-stage build para empaquetado y runtime ultraligero
# ============================================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Xms256m", "-Xmx768m", "-jar", "app.jar"]
