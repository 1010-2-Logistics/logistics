# 실제 서비스로 복사할 때 :template-service 부분을 해당 서비스명으로 바꾸세요.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :slack-service:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/template-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
