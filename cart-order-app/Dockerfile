# syntax=docker/dockerfile:1
#
# Expects the bootJar to be built on the host first:
#   ./gradlew :cart-order-app:bootJar
# (The image build does not run Gradle, to avoid depending on network access from within the build.)

# --- extract Spring Boot layers for better image caching ---
FROM eclipse-temurin:21-jre AS extract
WORKDIR /app
COPY cart-order-app/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
