FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app --home-dir /app --shell /usr/sbin/nologin app

COPY --from=build --chown=app:app /app/target/*.jar app.jar
COPY --chown=app:app docker-entrypoint.sh /app/docker-entrypoint.sh

RUN chmod 0555 /app/docker-entrypoint.sh

EXPOSE 8080

USER app

ENTRYPOINT ["/app/docker-entrypoint.sh"]
