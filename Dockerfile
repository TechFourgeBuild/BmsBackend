# Stage 1: Build stage using the full JDK
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage using a lightweight JRE (Saves massive RAM and Disk)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy ONLY the built JAR file from the build stage
COPY --from=build /app/target/BMSProject-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# CRITICAL JVM TUNING: SerialGC and MaxRAMPercentage keep it safely under 512MB
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xss512k", "-XX:MaxRAMPercentage=70.0", "-jar", "app.jar"]
