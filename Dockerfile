# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the application (skipping tests for speed in CI/CD)
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (default for Spring Boot)
EXPOSE 8080

# Environment variables will be injected by Railway
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
