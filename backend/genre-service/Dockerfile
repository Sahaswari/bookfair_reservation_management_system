# Change due to docker build issue
# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Fix DNS inside Docker build (prevents Maven "Temporary failure in name resolution")
# RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

# Copy pom.xml first (layer caching)
COPY pom.xml .

# Download dependencies (no retry block, much safer)
RUN mvn dependency:resolve -B && mvn dependency:resolve-plugins -B

# Copy the rest of the project
COPY src ./src

# Build the app
RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Fix DNS on runtime image too (optional but recommended)
# RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


# Stage 1: Build the application
# FROM maven:3.9.9-eclipse-temurin-17 AS build

# WORKDIR /app

# Copy pom.xml and download dependencies
# COPY pom.xml .

# Download dependencies with retry logic and without going offline
# This is more reliable than go-offline
# RUN mvn dependency:resolve dependency:resolve-plugins -B || \
#     mvn dependency:resolve dependency:resolve-plugins -B || \
#     mvn dependency:resolve dependency:resolve-plugins -B

# Copy source code and build
# COPY src ./src
# RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image
# FROM eclipse-temurin:17-jre-alpine

# WORKDIR /app

# Copy the JAR from build stage
# COPY --from=build /app/target/*.jar app.jar

# Expose the port
# EXPOSE 8082

# Set environment variables
# ENV JAVA_OPTS=""

# Run the application
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

