FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy pom files first for better layer caching
COPY pom.xml .
COPY common/pom.xml common/
COPY user_service/pom.xml user_service/
COPY order_service/pom.xml order_service/
COPY market_service/pom.xml market_service/

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY common/src common/src
COPY user_service/src user_service/src
COPY order_service/src order_service/src
COPY market_service/src market_service/src

# Build the application
RUN mvn clean package -DskipTests -B