# 1. Compilar la aplicación con Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Entorno ejecutable con servidor VNC / noVNC
FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:0

RUN apt-get update && apt-get install -y \
    openjdk-17-jre \
    xvfb \
    x11vnc \
    openbox \
    novnc \
    websockify \
    net-tools \
    && rm -rf /var/lib/apt-get/lists/*

WORKDIR /app

COPY --from=builder /app/target/automata-studio-1.0.0.jar app.jar
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

EXPOSE 10000

CMD ["/app/start.sh"]

