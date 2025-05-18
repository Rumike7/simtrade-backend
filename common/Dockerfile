FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/market-service-0.0.1-SNAPSHOT.jar market-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "market-service.jar"]
