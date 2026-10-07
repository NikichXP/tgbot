FROM gradle:9.8-jdk21 AS builder

WORKDIR /app

COPY settings.gradle.kts build.gradle.kts ./
COPY tg-bot-api/build.gradle.kts ./tg-bot-api/
COPY tg-bot-api/src ./tg-bot-api/src
COPY tg-bot-core/build.gradle.kts ./tg-bot-core/
COPY tg-bot-core/src ./tg-bot-core/src
COPY debug-bot/build.gradle.kts ./debug-bot/
COPY debug-bot/src ./debug-bot/src
COPY tg-bot-production/build.gradle.kts ./tg-bot-production/
COPY tg-bot-production/src ./tg-bot-production/src
RUN gradle build --no-daemon

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/tg-bot-production/build/libs/app.jar .

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
