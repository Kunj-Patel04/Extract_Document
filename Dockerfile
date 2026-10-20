# Step 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency definitions first to leverage Docker layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

# Copy source code and package jar (skip tests for faster build)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render assigns a dynamic port via the PORT environment variable (default 8080)
ENV PORT=8080
EXPOSE 8080

# Configure JVM memory limits for Render's free tier (512MB RAM)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xmx380m", "-jar", "app.jar"]