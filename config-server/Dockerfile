FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
COPY "target/config-server-0.0.1-SNAPSHOT.jar" app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
