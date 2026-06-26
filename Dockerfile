# Stage 2 — run the jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Back to normal! No memory limits needed on a larger instance.
ENTRYPOINT ["java", "-jar", "app.jar"]