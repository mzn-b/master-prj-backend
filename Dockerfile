##### BUILD #####
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /workspace

COPY pom.xml ./

COPY src src
RUN mvn -q -DskipTests package

###### RUN #####
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
