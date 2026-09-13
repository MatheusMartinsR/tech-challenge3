# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Imagem do Serviço de Notificações.
#
# O contexto de build é a RAIZ do repositório (ver docker-compose.yml), porque o
# módulo depende do `common` e do POM pai. Só este serviço e o `common` são
# compilados: o agendamento-service não entra na imagem — nem o código dele, nem
# suas dependências (Spring Security, JWT).
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Os POMs vêm primeiro para que a camada de dependências só seja refeita quando um
# POM mudar, e não a cada alteração de código. O POM do agendamento-service é
# copiado porque o POM pai o declara como módulo — sem ele o reator nem carrega.
COPY pom.xml ./
COPY common/pom.xml common/
COPY notificacao-service/pom.xml notificacao-service/
COPY agendamento-service/pom.xml agendamento-service/
# O `|| true` cobre o caso de o goal não resolver o módulo irmão ainda não
# compilado; o que faltar é baixado no passo de package abaixo.
RUN mvn -B -pl common,notificacao-service -am dependency:go-offline || true

COPY common/src common/src
COPY notificacao-service/src notificacao-service/src
RUN mvn -B -pl common,notificacao-service -am clean package -DskipTests

# ---------------------------------------------------------------------------
# Estágio 2 — runtime
# Só o JRE e o JAR: a imagem final não carrega Maven, código-fonte nem o .m2.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuário sem privilégios: nada aqui precisa de root.
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build --chown=spring:spring /build/notificacao-service/target/*.jar app.jar

USER spring

EXPOSE 8081

# Espaço para ajustes de JVM sem reconstruir a imagem (ex.: -Xmx256m).
ENV JAVA_OPTS=""

# Este serviço não expõe API de negócio: o /actuator/health existe só para o
# health check do container e para o depends_on do compose.
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
