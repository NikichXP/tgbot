FROM gradle:9.3-jdk21 AS builder

WORKDIR /app

COPY build.gradle.kts .

COPY src ./src
RUN gradle build --no-daemon

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar .

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
