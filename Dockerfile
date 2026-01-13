# Multi-stage build for SlskdDownload
# Stage 1: Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR file from builder stage
# Note: artifactId in pom.xml is "Slskd" so JAR name is Slskd-0.0.1.jar
COPY --from=builder /build/target/Slskd-0.0.1.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
