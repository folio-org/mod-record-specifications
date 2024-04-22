FROM folioci/alpine-jre-openjdk17:latest

USER root
RUN apk upgrade --no-cache
USER folio

ENV APP_FILE mod-record-specifications-fat.jar
ARG JAR_FILE=./mod-record-specifications-server/target/mod-record-specifications-server-1.0.0-SNAPSHOT.jar
COPY ${JAR_FILE} ${JAVA_APP_DIR}/${APP_FILE}

EXPOSE 8081
