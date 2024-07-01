# Use the official Maven image with Java 17
FROM maven:3.8.4-openjdk-17-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file to the working directory
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline

# Copy the application source code to the working directory
COPY . .

# Build the application
RUN mvn package

# Expose the port on which your application runs (adjust if needed)
EXPOSE 8080

# Set the command to run your application
CMD ["java", "-jar", "target/websocket-application-0.0.1-SNAPSHOT.jar"]