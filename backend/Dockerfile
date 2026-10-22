# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Copy source code
COPY src ./src

# Build the application
RUN gradle buildFatJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*-all.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
