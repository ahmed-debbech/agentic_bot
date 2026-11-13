
# ==========================================
# Stage 1: Build Spring Boot application
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy Maven configuration first (better caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy application source code
COPY src ./src

# Build executable JAR
RUN mvn clean package -DskipTests


# ==========================================
# Stage 2: Run Spring Boot application
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create conversation directory
RUN mkdir -p /conv
RUN mkdir -p /prompts

# Copy generated JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Spring Boot port
EXPOSE 9011

# Run application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.additional-location=file:/app/src/main/resources/application.properties"]