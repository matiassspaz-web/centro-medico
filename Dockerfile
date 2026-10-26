# Etapa 1: compilar el jar
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
COPY src/ src/
RUN mvn -B -DskipTests package

# Etapa 2: imagen final, solo con el runtime y el jar
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/centro-medico-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
