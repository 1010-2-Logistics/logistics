# 실제 서비스로 복사할 때 :user-service 부분을 해당 서비스명으로 바꾸세요.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :user-service:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/user-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
