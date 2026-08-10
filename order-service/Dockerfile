FROM alpine:3 AS select
WORKDIR /app
COPY order-service/build/libs/*.jar ./
RUN rm -f *-plain.jar && mv *.jar app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=select /app/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
