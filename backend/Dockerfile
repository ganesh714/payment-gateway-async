FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/payment-gateway-0.0.1-SNAPSHOT.jar app.jar
# The worker runs the same app, but you can use profiles to distinguish behavior if needed
ENTRYPOINT ["java", "-jar", "app.jar"]