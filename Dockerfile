FROM maven:3-eclipse-temurin-19-alpine AS build
WORKDIR /
COPY /src /src
ADD src/main/resources/images /src/main/resources/images
COPY pom.xml /
RUN mvn -f /pom.xml clean package -DskipTests

FROM openjdk:19-alpine
WORKDIR /
COPY /src /src
ADD src/main/resources/images /src/main/resources/images
COPY --from=build /target/*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]

EXPOSE 8080