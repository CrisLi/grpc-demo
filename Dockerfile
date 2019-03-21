FROM openjdk:11.0.2-jdk-slim-stretch

VOLUME /tmp

WORKDIR /usr/app

COPY target/*.jar app.jar

EXPOSE 8080
EXPOSE 8085

ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=prod", "app.jar"]
