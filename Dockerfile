FROM maven:latest AS build

WORKDIR /app

ADD . .

RUN mvn install -DskipTests

FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=Build /app/target/pixza-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=envs", "pixza-0.0.1-SNAPSHOT.jar"]