FROM gradle:jdk11 as builder

WORKDIR /app

COPY build.gradle.kts .

COPY src ./src
RUN gradle clean build --no-daemon

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar .

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
