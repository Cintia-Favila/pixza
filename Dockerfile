FROM maven:latest AS build

WORKDIR /app

ADD . .

RUN mvn install -DskipTests

FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=build /app/target/pixza-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["java", "-Dspring.profiles.active=envs", "-jar", "pixza-0.0.1-SNAPSHOT.jar"]
