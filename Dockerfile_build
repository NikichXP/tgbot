FROM gradle:jdk17 as builder

WORKDIR /app

COPY build.gradle.kts .

COPY src ./src
RUN gradle build --no-daemon

FROM openjdk:17-slim

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar .

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
