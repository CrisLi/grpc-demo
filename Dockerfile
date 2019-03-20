FROM openjdk:8-jdk-alpine

VOLUME /tmp

WORKDIR /usr/app

COPY target/*.jar app.jar

EXPOSE 8080
EXPOSE 8085

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=prod", "app.jar"]
