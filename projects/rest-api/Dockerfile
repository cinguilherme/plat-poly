# Use OpenJDK 19 base image
FROM openjdk:19

# Set the working directory inside the container
WORKDIR /app

# Copy the uberjar into the container
COPY target/rest-api.jar /app/app.jar

# Expose the port your app runs on
EXPOSE 8890

# Run the uberjar
CMD ["java", "-jar", "/app/app.jar"]
