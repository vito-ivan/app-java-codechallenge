# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project file and the source code
# COPY pom.xml /app
# COPY src /app/src

# Package the application
# RUN ./mvnw clean install -DskipTests

# Copy the packaged JAR file to the working directory
COPY target/*.jar app.jar

# Exponer el puerto en el que la aplicación se ejecutará (cambiar si es necesario)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
