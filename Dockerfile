FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# Stage 2 — run the jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Limit Java's memory usage so it survives on small cloud instances
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-Xss512k", "-jar", "app.jar"]