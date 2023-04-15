FROM openjdk:19-alpine
ADD src/main/resources/images /home/qreol/Desktop/hubr/src/main/resources/images
ADD /target/Hubr-0.0.1-SNAPSHOT.jar hubr
ENTRYPOINT ["java", "-jar","hubr"]
EXPOSE 8080