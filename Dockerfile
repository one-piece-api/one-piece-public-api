# syntax=docker/dockerfile:1
# Runtime-only image: the jar is built beforehand (./gradlew bootJar), not inside this
# Dockerfile - see scripts/build-image.sh for local dev. Same pattern as the other services
# (keeps the GitHub Packages credentials confined to wherever Gradle actually runs).
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY build/libs/*.jar app.jar
USER app
# 8080: the API; 8081: actuator (probes), never routed outside the cluster.
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
