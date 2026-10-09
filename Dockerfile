FROM gradle:9.5-jdk21 AS builder

WORKDIR /app

COPY settings.gradle.kts build.gradle.kts ./
COPY tg-bot-api/build.gradle.kts ./tg-bot-api/
COPY tg-bot-api/src ./tg-bot-api/src
COPY tg-bot-core/build.gradle.kts ./tg-bot-core/
COPY tg-bot-core/src ./tg-bot-core/src
COPY debug-bot/build.gradle.kts ./debug-bot/
COPY debug-bot/src ./debug-bot/src
COPY karma-bot/build.gradle.kts ./karma-bot/
COPY karma-bot/src ./karma-bot/src
COPY santa-bot/build.gradle.kts ./santa-bot/
COPY santa-bot/src ./santa-bot/src
COPY child-care-bot/build.gradle.kts ./child-care-bot/
COPY child-care-bot/src ./child-care-bot/src
COPY summary-bot/build.gradle.kts ./summary-bot/
COPY summary-bot/src ./summary-bot/src
COPY voicepad-bot/build.gradle.kts ./voicepad-bot/
COPY voicepad-bot/src ./voicepad-bot/src
COPY okx-bot/build.gradle.kts ./okx-bot/
COPY okx-bot/src ./okx-bot/src
COPY tg-bot-production/build.gradle.kts ./tg-bot-production/
COPY tg-bot-production/src ./tg-bot-production/src
RUN gradle build --no-daemon

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/tg-bot-production/build/libs/app.jar .

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
