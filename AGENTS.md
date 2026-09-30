# AGENTS.md — tg-bot

Telegram bot backend service supporting multiple bots, child-care tracking, karma, AI voice transcription & summaries, and Discord webhooks.

## Technology Stack

- **Language**: Kotlin 2.x, JDK 21 (Amazon Corretto)
- **Framework**: Spring Boot 3.5.x (WebFlux, Reactive, Coroutines)
- **Database**: MongoDB (`spring-boot-starter-data-mongodb`)
- **HTTP Client**: Ktor Client (`io.ktor:ktor-client-cio-jvm`)
- **Build Tool**: Gradle (Kotlin DSL, `./gradlew`)
- **Cache/session store**: Redis (reactive, dashboard tokens)
- **Tests**: JUnit 5 only (`org.junit.jupiter.api.Test`; `kotlin.test` asserts via `kotlin-test-junit5`). JUnit 4 and
  the vintage engine are excluded in `build.gradle.kts` — don't add them back.

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
| `APP_TRACER_*` | Tracer configs (`STORE`, `TTL`, `CAPACITY`, `TOKEN`) | `true`, `24`, `100`, `null` |
| `OPENROUTER_*` | OpenRouter AI configs (`API_KEY`, `DEFAULT_MODEL`, `BASE_URL`, `REFERER`, `TITLE`, `TRANSCRIPTION_MODEL`) | `openrouter/auto`, `openai/whisper-1` |
| `OKX_COLLECTOR_URL` | okx-collector base URL for `/prices` (`/watch` goes via RabbitMQ `okx.watch.*` queues); bot needs the `okx` feature | `http://localhost:8080` |
| `DISCORD_PUBLIC_KEY` | Public key for Discord interaction signature verification | `null` |
| `REDIS_*` | Redis (`HOST`, `PORT`, `USERNAME`, `PASSWORD`, `DATABASE`) — dashboard access tokens | `localhost`, `6379`, db `0` |
| `APP_DASHBOARD_TELEGRAM_CLIENT_ID` | "Log In with Telegram" (OIDC) Client ID from @BotFather (the client secret is not used); also used by `/oauth/*` | — |
| `APP_DASHBOARD_ALLOWED_ORIGINS` | CORS origins allowed to call `/admin/**` with credentials | `http://localhost:5173` |
| `APP_DASHBOARD_ACCESS_TOKEN_TTL_MINUTES` | Dashboard access token lifetime | `15` |
| `APP_DASHBOARD_SECURE_COOKIE` | `Secure` flag of the refresh-token cookie (disable only for local http) | `true` |

## Web dashboard (`dashboard/`)

React admin UI (Vite + TS + MUI + TanStack Query) at `dashboard.tgbot.nikichxp.xyz`; see
[`dashboard/README.md`](dashboard/README.md). Backend side lives in `com.nikichxp.tgbot.dashboard`
(`api`, `config`, `service`, `repository`, `connector`, `dto`, `entity`, `error`):

- `/admin/auth/*` (public): config, login nonce, Telegram login, refresh, logout. Only `APP_ADMIN_ID` is let in.
- Login is "Log In with Telegram" via OpenID Connect: `telegram-login.js` popup returns an `id_token` (JWT) to the
  frontend; the backend verifies it against Telegram's JWKS (`iss`, `aud` = client id, `exp`) and a one-time
  nonce (Redis `dashboard:nonce:*`, 10 min) issued by `POST /admin/auth/nonce`. The numeric user id is the `id` claim.
- `/admin/*` (Bearer access token): `me`, `features`, `bots` (list/add), `bots/{name}/features`, `oauth-clients`
  (list/add/rotate secret/delete), `users` (search), `users/{userId}/avatar`, `lists` (CRUD: `POST` → 201,
  `PATCH` description), `lists/{name}/members/{userId}` (`PUT`/`DELETE`). Auth filter and error mapping are shared via
  `DashboardApiSupport`.
- Access tokens: opaque, Redis (`dashboard:access:<sha256>`, TTL). Refresh tokens: opaque, HttpOnly cookie,
  SHA-256 stored in Mongo `dashboardSessions`, no expiry, revoked on logout.
- @BotFather → bot → Login Widget must list `https://dashboard.tgbot.nikichxp.xyz` as a trusted origin and
  `https://dashboard.tgbot.nikichxp.xyz/` as a redirect URI (the login page is always served on `/`).
- Features offered in the UI come from `Features.ALL` — add new feature constants there too.

## Telegram login for other services (`oauth/`)

tg-bot is an OAuth 2.0 authorization-code provider on top of "Log In with Telegram", so other services
(any other service in the workspace) don't implement Telegram login themselves. Integrator guide:
[`docs/oauth-integration.md`](docs/oauth-integration.md). Code: `com.nikichxp.tgbot.oauth`.

- Clients (`client_id`, name, SHA-256 of secret, redirect URIs) live in Mongo `oauthClients`; managed from the
  dashboard (`/admin/oauth-clients`, `DashboardOAuthClientService`). Secret is shown once; can be rotated.
- `GET /oauth/authorize?client_id&redirect_uri&state&response_mode=query|web_message` — hosted login page
  (`resources/oauth/authorize.html`). Unknown client / unregistered redirect URI → 400, never a redirect.
  Issues a nonce (Redis `oauth:request:*`, 15 min) bound to client/redirect/state. `web_message` pages may be framed
  only by the client's redirect-URI origins (CSP `frame-ancestors`), `query` pages not at all.
- `POST /oauth/login {idToken}` — called by that page; verifies the Telegram `id_token` (same
  `TelegramIdTokenVerifier`/client id as the dashboard), consumes the nonce, issues a one-time code
  (Redis `oauth:code:<sha256>`, 60 s).
- `POST /oauth/token` (form, `client_secret_post` or `client_secret_basic`) — exchanges the code for
  `{ user: {id, name, username, photoUrl}, authTime }`. OAuth-style errors.
- `GET /oauth/embed.js` — `TgBotAuth.mount(el, {clientId, redirectUri, state, onCode})` iframe helper.
- Any Telegram user may log in here (unlike the dashboard); authorization is the client's job.
- @BotFather → Login Widget must also list `https://api.tgbot.nikichxp.xyz` (origin) and
  `https://api.tgbot.nikichxp.xyz/oauth/authorize` (redirect URI): telegram-login.js uses the page URL without query.

## People registry and people lists (`com.nikichxp.tgbot.people`)

- `KnownUserTrackingHandler` (all bots, no feature needed) records every non-bot user seen in an update
  (`from` and the replied-to author) into Mongo `knownUsers`: profile, bots that saw them, chats (`chats.<chatId>`
  with title/type/last seen). Writes are throttled per user+bot+chat (10 min) unless the profile or chat changed.
  Optional profile fields (`languageCode`, `isPremium`) are only overwritten when present in the update.
- `peopleLists` (Mongo): named lists of Telegram user ids, edited in the dashboard (`/admin/lists/**`).
  Code references a list by its `name`; use `PeopleListService.isMember(name, userId)` instead of hardcoded ids
  or env vars (e.g. `APP_TRUSTED_USERS` for summary is to be migrated to a list).
- Avatars: `/admin/users/{userId}/avatar` downloads the current profile photo via any bot that saw the user
  (`getUserProfilePhotos` → `getFile`), cached in Redis `people:avatar:<id>` (12 h; "no avatar" for 1 h → 204).

## Deployment & Infrastructure

- **Cluster Manifests**: Managed in [`infra-scripts/manifests/tgbot/`](../infra-scripts/manifests/tgbot/).
- **Ingress**: `api.tgbot.nikichxp.xyz` → backend, `dashboard.tgbot.nikichxp.xyz` → dashboard (`31-ingress-tgbot.yaml`);
  legacy `bot.nikichxp.xyz` → backend (`30-ingress.yaml`), kept while `APP_WEBHOOK` still points to it.
- **Secrets**: `tgbot-auth` Secret in namespace `tgbot`.
- **Health Probes**:
  - Liveness: `/actuator/health/liveness`
  - Readiness: `/actuator/health/readiness`
- **Docker Images**: Built via GitHub Actions: `kraken.nikichxp.xyz/tgbot:latest` (`github-build-push.yml`, ignores
  `dashboard/**`) and `kraken.nikichxp.xyz/tgbot-dashboard:latest` (`dashboard.yml`, only `dashboard/**`).
- **Backend image layers**: `Dockerfile_copy` (used by CI) splits the boot jar into Spring Boot layers
  (`dependencies` ~55 MB, `spring-boot-loader`, `snapshot-dependencies`, `application` ~1.3 MB) with pinned mtimes,
  so a code-only push re-uploads/pulls only the application layer. Keep jar builds reproducible (Gradle 9 default).

## Conventions & Best Practices

1. **Mandatory Build Verification**: **Always** run `./gradlew build` (or `./gradlew test`) after making any code, configuration, or dependency changes to verify that the project compiles and passes all tests before completing a task.
2. **Reactive & Coroutines**: Use Kotlin coroutines (`suspend`, `coRouter`, `awaitBody`, `bodyValueAndAwait`) instead of blocking calls.
3. **Configuration**: Keep `AppConfig` safe with default parameter values so optional features don't crash startup if an env var is omitted.
4. **Secrets**: Never commit `.env` or hardcode tokens/credentials in code or manifests.
5. **Package layout by role**: don't mix roles in one file or package. Within a feature package
   (e.g. `com.nikichxp.tgbot.dashboard`):
   - `dto/` — DTOs (request/response bodies and internal data carriers), never next to services;
   - `service/` — services (business logic);
   - `repository/` — repositories (MongoDB/Redis access); services don't call `MongoTemplate`/Redis directly;
   - `connector/` — clients of external systems (HTTP APIs etc.);
   - `entity/` — persisted documents, `error/` — exceptions, `api/` — routers/controllers, `config/` — Spring config;
   - `converters/` — Spring `Converter` beans for object → object mapping (entity → DTO, wire DTO → model),
     used through `ConversionService` instead of hand-written `toDto()`/`toModel()` helpers in services.

   Exception: an interface following the `IFooService` pattern and its implementations
   (`FooServiceBarImpl`) may live together in one package. Applies to new and modified code; legacy
   code is migrated when touched.
6. **Self-explanatory code, no comments**: code must be understandable without comments — express intent
   through names (functions, variables, constants) and tests instead. No KDoc/Javadoc/JSDoc, no comments
   explaining what code does. Never touch `TODO` comments. Tool directives (`oxlint-disable…`,
   `/// <reference …>`) are not comments in this sense.
7. **Constructor style**: a primary constructor with parameters is always multi-line, one parameter per
   line, even when there is only one (`class Foo(\n    private val bar: Bar\n) {`).
8. **REST paths**: plural nouns for collections (`/admin/users`, `/admin/lists/{name}/members/{userId}`),
   `POST` creates (201 + `Location`), `PATCH` for partial updates, `PUT` for idempotent set, `DELETE` removes.
9. **`Update` is a wire DTO**: `core.dto.Update` may only be used to (a) read/deserialise the raw
   Telegram request and (b) map that data into typed models (`UpdateContext` via
   `TgUpdateContextMapper`, which delegates each wire-DTO → model mapping to a Spring
   `Converter` bean in `core/converters` via `ConversionService`, or `LoggedMessage`'s typed fields via `SummaryMessageStorageService`'s
   one-time legacy-record migration). It must never be stored on a domain entity or read again after
   mapping. Handlers and business logic must use `UpdateContext` and its typed models
   (`UserModel`/`ReplyModel`/`MessageModel`/`CallbackModel`) instead.

