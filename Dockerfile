# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies first to leverage Docker cache
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src src

# Package the application, skipping tests for faster builds
RUN mvn clean package -DskipTests -B

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the port the application will run on
EXPOSE 8080

# Health check (opcional pero recomendado)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]