# Use an official Maven image to build the Spring Boot app
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the Spring Boot app
COPY src ./src
RUN mvn clean package -DskipTests

# Use an official OpenJDK image to run the Spring Boot app
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/due-management-0.0.1-SNAPSHOT.jar .

# Expose the port
EXPOSE 8080

# Run the Spring Boot app
CMD ["java", "-jar", "due-management-0.0.1-SNAPSHOT.jar"]
