# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml ./
COPY common/pom.xml common/
COPY notificacao-service/pom.xml notificacao-service/
COPY agendamento-service/pom.xml agendamento-service/
RUN mvn -B -pl common,notificacao-service -am dependency:go-offline || true

COPY common/src common/src
COPY notificacao-service/src notificacao-service/src
RUN mvn -B -pl common,notificacao-service -am clean package -DskipTests
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build --chown=spring:spring /build/notificacao-service/target/*.jar app.jar

USER spring

EXPOSE 8081
ENV JAVA_OPTS=""
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
