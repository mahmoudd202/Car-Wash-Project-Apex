FROM eclipse-temurin:21-jre-alpine
RUN adduser -D app
COPY target/carwash.jar /app.jar
USER app
ENTRYPOINT ["java", "-jar", "/app.jar"]
