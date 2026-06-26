# Stage 1 — build the jar
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon -x test

# Stage 2 — run the jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Back to normal! No memory limits needed on a larger instance.
ENTRYPOINT ["java", "-jar", "app.jar"]