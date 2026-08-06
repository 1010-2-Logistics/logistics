FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN ./gradlew :product-service:bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/product-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
