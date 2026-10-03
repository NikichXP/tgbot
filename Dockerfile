FROM gradle:9.5-jdk21 AS builder

WORKDIR /app

COPY settings.gradle.kts build.gradle.kts ./
COPY tg-bot-production/build.gradle.kts ./tg-bot-production/
COPY tg-bot-production/src ./tg-bot-production/src
RUN gradle build --no-daemon

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/tg-bot-production/build/libs/app.jar .

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
