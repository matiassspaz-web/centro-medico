# Etapa 1: compilar el jar con el Maven Wrapper
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package

# Etapa 2: imagen final, solo con el runtime y el jar
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/centro-medico-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
