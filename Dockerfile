# 1. Usamos ESTRICTAMENTE Java 17 para compilar
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

ADD . .

# 2. Compilamos
RUN mvn clean install -DskipTests

# 3. Levantamos el servidor con la misma versión de Java 17
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=build /app/target/pixza-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java", "-jar", "pixza-0.0.1-SNAPSHOT.jar"]