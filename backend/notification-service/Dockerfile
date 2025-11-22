# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .

# Download dependencies with retry logic and without going offline
# This is more reliable than go-offline
RUN mvn dependency:resolve dependency:resolve-plugins -B || \
    mvn dependency:resolve dependency:resolve-plugins -B || \
    mvn dependency:resolve dependency:resolve-plugins -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8083

# Set environment variables
ENV JAVA_OPTS=""

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
