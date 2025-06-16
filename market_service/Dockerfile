FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=simtrade-builder /build/market_service/target/market_service-0.0.1-SNAPSHOT.jar market_service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "market_service.jar"]
