# Build stage — Java 21 (matches pom.xml)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B -DskipTests package \
    && cp target/calistrack-backend-*.jar /app/app.jar

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/app.jar app.jar

# Render injects PORT; Spring already uses server.port=${PORT:8084}
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
