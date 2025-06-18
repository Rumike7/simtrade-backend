FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=simtrade-builder /app/order_service/target/order_service-0.0.1-SNAPSHOT.jar order_service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "order_service.jar"]
