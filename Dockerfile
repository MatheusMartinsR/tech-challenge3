# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Estágio 1 — build
# Compila com JDK 21 (o projeto tem release 17; o JDK maior é compatível).
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# O pom vem sozinho primeiro para que a camada de dependências só seja refeita
# quando o próprio pom mudar, e não a cada alteração de código.
COPY pom.xml ./
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Estágio 2 — runtime
# Só o JRE e o JAR: a imagem final não carrega Maven, código-fonte nem o .m2.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuário sem privilégios: nada aqui precisa de root.
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build --chown=spring:spring /build/target/*.jar app.jar

USER spring

EXPOSE 8080

# Espaço para ajustes de JVM sem reconstruir a imagem (ex.: -Xmx256m).
ENV JAVA_OPTS=""

# Exige o spring-boot-starter-actuator; /actuator/health é liberado no SecurityConfig.
# O wget vem do busybox da imagem alpine, sem instalar nada.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
