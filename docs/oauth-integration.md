# Вход через Telegram для других сервисов (tg-bot как OAuth-провайдер)

tg-bot берёт на себя весь флоу "Log In with Telegram" (OIDC): показывает кнопку, открывает попап Telegram,
проверяет `id_token` (подпись по JWKS Telegram, `iss`, `aud`, `exp`, одноразовый nonce) и отдаёт сервису-клиенту
только проверенного пользователя. Сервису не нужно регистрироваться в @BotFather и разбираться с JWT.

Схема — OAuth 2.0 Authorization Code (конфиденциальный клиент):

```
Браузер (сайт сервиса A)          tg-bot (api.tgbot.nikichxp.xyz)             Бэкенд сервиса A
  iframe /oauth/authorize ──────▶ страница с кнопкой, nonce в Redis
  клик → попап Telegram ─────────▶ POST /oauth/login {idToken}
                                   проверка JWT + nonce → одноразовый code (60 с)
  ◀── postMessage {code, state} (или redirect на redirect_uri?code&state)
  code ───────────────────────────────────────────────────────────────────▶
                                   POST /oauth/token (code + client_secret) ◀──
                                   ──▶ { user: { id, name, username, photoUrl }, authTime }
```

tg-bot **только аутентифицирует**: пускает любого пользователя Telegram. Кого пускать и какие сессии заводить —
решает сервис A.

## 1. Регистрация клиента

Dashboard → **OAuth** → «Добавить»: `client_id` (например `pancakes`), название (видно пользователю) и список redirect URI
(по одному на строку, `https://…`, `http://` — только для `localhost`). Секрет показывается один раз — положить его
в секреты сервиса (`05-secret.yaml`). Потерялся — «Новый секрет».

Origin'ы redirect URI — это одновременно и список сайтов, которым разрешено встраивать виджет во `<iframe>`
(`Content-Security-Policy: frame-ancestors`), и адрес, куда уходит `postMessage`.

## 2. Фронтенд: виджет во фрейме

```html
<div id="tg-login"></div>
<script src="https://api.tgbot.nikichxp.xyz/oauth/embed.js"></script>
<script>
  const state = crypto.randomUUID()
  sessionStorage.setItem('tgbot-oauth-state', state)
  TgBotAuth.mount('#tg-login', {
    clientId: 'pancakes',
    redirectUri: 'https://pancakes.example.com/auth/telegram',
    state,
    onCode: ({ code, state }) =>
      fetch('/api/auth/telegram', { method: 'POST', body: JSON.stringify({ code, state }) }),
  })
</script>
```

`embed.js` вставляет iframe (`width: 100%`, `height: 110px`, можно переопределить опциями `width`/`height`),
принимает `postMessage` только от origin'а tg-bot и только от своего iframe, и сверяет `state`.
`mount` возвращает `{ iframe, destroy() }`.

Без `embed.js` — то же самое вручную: iframe на
`/oauth/authorize?client_id=…&redirect_uri=…&state=…&response_mode=web_message` и слушатель `message`
с проверкой `event.origin === 'https://api.tgbot.nikichxp.xyz'` и `event.data.type === 'tgbot-oauth'`.
Та же страница работает и во всплывающем окне (`window.open`) — тогда сообщение уходит в `window.opener`.

### Вариант без фрейма: редирект

Перенаправить пользователя на
`https://api.tgbot.nikichxp.xyz/oauth/authorize?client_id=pancakes&redirect_uri=<urlencoded>&state=<random>`
(`response_mode` по умолчанию `query`). После входа браузер вернётся на `redirect_uri?code=…&state=…`.

## 3. Бэкенд: обмен code на пользователя

```http
POST https://api.tgbot.nikichxp.xyz/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=<code>&redirect_uri=<тот же redirect_uri>&client_id=pancakes&client_secret=<secret>
```

Вместо `client_id`/`client_secret` в теле можно передать `Authorization: Basic base64(client_id:client_secret)`.

Ответ `200`:

```json
{ "user": { "id": 34080460, "name": "Nik", "username": "nikichxp", "photoUrl": "https://…" }, "authTime": 1790000000 }
```

`user.id` — числовой Telegram user id, стабильный идентификатор пользователя.

Ошибки — в формате OAuth: `{"error": "...", "error_description": "..."}`
(`invalid_client` → 401, `invalid_grant` / `invalid_request` / `unsupported_grant_type` → 400).

Правила:
- `code` одноразовый и живёт 60 секунд; `redirect_uri` должен совпадать с тем, что был в `/oauth/authorize`.
- `state` сервис генерирует сам и проверяет у себя (защита от CSRF / подмены логина).
- `client_secret` — только на бэкенде, никогда во фронтенде.

## Ограничения

- Страница входа живёт 15 минут (nonce в Redis), потом сама перезагружается, если вход не начат.
- Telegram пускает только origin'ы/redirect URI, зарегистрированные в @BotFather для бота tg-bot:
  `https://api.tgbot.nikichxp.xyz` и `https://api.tgbot.nikichxp.xyz/oauth/authorize` (это настройка tg-bot,
  клиентам её делать не нужно).
