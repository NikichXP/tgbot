# AGENTS.md — tg-bot

Telegram bot backend service supporting multiple bots, child-care tracking, karma, AI voice transcription & summaries, warehouse integration, and Discord webhooks.

## Technology Stack

- **Language**: Kotlin 2.x, JDK 21 (Amazon Corretto)
- **Framework**: Spring Boot 3.5.x (WebFlux, Reactive, Coroutines)
- **Database**: MongoDB (`spring-boot-starter-data-mongodb`)
- **HTTP Client**: Ktor Client (`io.ktor:ktor-client-cio-jvm`)
- **Build Tool**: Gradle (Kotlin DSL, `./gradlew`)

## Configuration Architecture

`tg-bot` is configured purely through **environment variables** and standard Spring Boot property bindings (no Spring Cloud Config or external config server).

### Local Development (.env)
- The application automatically imports `.env` from the project root via `spring.config.import: optional:file:.env[.properties]`.
- Template file: [`sample.env`](sample.env). Copy it to `.env` and fill in secrets:
  ```bash
  cp sample.env .env
  ```
- **Local flags**:
  - `APP_LOCAL_ENV=true` and `APP_SUSPEND_BOT_REGISTERING=true` prevent the application from setting remote webhooks on startup when developing locally.

### Key Environment Variables

| Variable | Description | Default |
|---|---|---|
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/tg-bot` |
| `MONGODB_DATABASE` | MongoDB database name | `tg-bot` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ username | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `APP_ADMIN_ID` | Telegram User ID of the bot admin/owner | `0` (loaded from `.env` / secret) |
| `APP_ADMIN_BOT` | Name of the primary admin bot (bot entities/tokens are loaded from MongoDB) | `null` |
| `APP_WEBHOOK` | Base webhook URL registered with Telegram API | `https://bot.nikichxp.xyz/handle` |
| `APP_LOCAL_ENV` | When true, skips webhook registration | `false` |
| `APP_SUSPEND_BOT_REGISTERING` | When true, suspends bot webhook registration | `false` |
| `APP_WAREHOUSE_URL` | Warehouse service URL | `https://warehouse.nikichxp.xyz/storage` |
| `APP_TRACER_*` | Tracer configs (`STORE`, `TTL`, `CAPACITY`, `TOKEN`) | `true`, `24`, `100`, `null` |
| `OPENROUTER_*` | OpenRouter AI configs (`API_KEY`, `DEFAULT_MODEL`, `BASE_URL`, `REFERER`, `TITLE`, `TRANSCRIPTION_MODEL`) | `openrouter/auto`, `openai/whisper-1` |
| `DISCORD_PUBLIC_KEY` | Public key for Discord interaction signature verification | `null` |

## Deployment & Infrastructure

- **Cluster Manifests**: Managed in [`infra-scripts/manifests/tgbot/`](../infra-scripts/manifests/tgbot/).
- **Ingress**: `bot.nikichxp.xyz` routed via Traefik to port `8080` (`/handle/{bot}`).
- **Secrets**: `tgbot-auth` Secret in namespace `tgbot`.
- **Health Probes**:
  - Liveness: `/actuator/health/liveness`
  - Readiness: `/actuator/health/readiness`
- **Docker Image**: Built via GitHub Actions and published to `kraken.nikichxp.xyz/tgbot:latest`.

## Conventions & Best Practices

1. **Mandatory Build Verification**: **Always** run `./gradlew build` (or `./gradlew test`) after making any code, configuration, or dependency changes to verify that the project compiles and passes all tests before completing a task.
2. **Reactive & Coroutines**: Use Kotlin coroutines (`suspend`, `coRouter`, `awaitBody`, `bodyValueAndAwait`) instead of blocking calls.
3. **Configuration**: Keep `AppConfig` safe with default parameter values so optional features don't crash startup if an env var is omitted.
4. **Secrets**: Never commit `.env` or hardcode tokens/credentials in code or manifests.

