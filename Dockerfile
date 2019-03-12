FROM openjdk:8-jdk-alpine

VOLUME /tmp

ARG JAR_FILE
COPY target/*.jar app.jar

EXPOSE 8080
EXPOSE 8085

ENTRYPOINT ["java","-jar","/app.jar"]
