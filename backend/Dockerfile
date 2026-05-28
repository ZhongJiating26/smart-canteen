FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG SERVICE_NAME
WORKDIR /workspace

COPY . .
RUN mvn -s /workspace/maven-settings.xml -q -pl ${SERVICE_NAME} -am -DskipTests package

FROM eclipse-temurin:17-jre

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}
WORKDIR /app

COPY --from=build /workspace/${SERVICE_NAME}/target/${SERVICE_NAME}-1.0-SNAPSHOT.jar /app/service.jar

EXPOSE 9000 9001 9002 9003 9004
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
