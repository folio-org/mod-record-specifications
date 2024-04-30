FROM folioci/alpine-jre-openjdk17:latest

USER root
RUN apk upgrade --no-cache
USER folio

ENV APP_FILE mod-record-specifications-server-fat.jar
ENV JAR_FILE=mod-record-specifications-server/target/${APP_FILE}
COPY ${JAR_FILE} ${JAVA_APP_DIR}/${APP_FILE}

EXPOSE 8081
