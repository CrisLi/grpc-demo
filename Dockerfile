FROM openjdk:11.0.2-jdk-slim-stretch

VOLUME /tmp

WORKDIR /usr/app

COPY target/*.jar app.jar
COPY docker-entrypoint.sh docker-entrypoint.sh

EXPOSE 8080
EXPOSE 8085

ENV JAVA_OPTS="-Xmx2048m -Xms2048m -Dspring.profiles.active=prod"

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
