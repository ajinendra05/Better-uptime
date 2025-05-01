# Build stage
FROM eclipse-temurin:17-jdk as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


# FROM eclipse-temurin:17-jdk
# WORKDIR /app
# COPY . .
# RUN ./mvnw clean package -DskipTests
# CMD ["java", "-jar", "target/your-app-name.jar"]  # Replace with your JAR name