FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=simtrade-builder /app/user_service/target/user_service-0.0.1-SNAPSHOT.jar user_service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "user_service.jar"]
