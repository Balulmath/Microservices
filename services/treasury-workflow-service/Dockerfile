FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
COPY services/treasury-workflow-service/pom.xml services/treasury-workflow-service/pom.xml
RUN mvn -B -pl services/treasury-workflow-service -am dependency:go-offline
COPY services services
RUN mvn -B -pl services/treasury-workflow-service -am package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/services/treasury-workflow-service/target/treasury-workflow-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
