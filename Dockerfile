#Reference image
FROM eclipse-temurin:21-jdk

ENV JAVA_OPTS="--enable-preview"

ARG JAR_FILE=target/eventsaggregation-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} eventsaggregation-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /eventsaggregation-0.0.1-SNAPSHOT.jar" ]