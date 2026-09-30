# tg-bot dashboard

Web admin UI for tg-bot: manage bots and their features. Only the Telegram user `APP_ADMIN_ID` can log in.

Stack: Vite + React + TypeScript, MUI (components), TanStack Query (server state).

## Auth

- **Login**: ["Log In with Telegram"](https://core.telegram.org/bots/telegram-login) (OpenID Connect).
  The page gets a one-time nonce (`POST /admin/auth/nonce`), `telegram-login.js` opens the Telegram popup and
  returns an `id_token` (JWT), which goes to `POST /admin/auth/telegram`. The backend verifies the JWT signature
  (Telegram JWKS), issuer, audience (client id), expiry and nonce, and checks the user is the admin.
- **Access token** (15 min): in `sessionStorage`, sent as `Authorization: Bearer …`; stored in Redis on the backend.
- **Refresh token** (no expiry, revoked on logout): HttpOnly cookie on `/admin/auth`; its hash is stored in
  MongoDB (`dashboardSessions`). On a 401 the client calls `/admin/auth/refresh` once and retries.

Telegram only accepts origins/redirect URIs registered in @BotFather → bot → Login Widget
(`https://dashboard.tgbot.nikichxp.xyz` and `https://dashboard.tgbot.nikichxp.xyz/`), so login can't be done from
`localhost`. The login page is always shown on `/` because the page URL is used as the redirect URI.

## Development

```bash
npm install
npm run dev     # http://localhost:5173, /admin is proxied to the backend on :8080
npm run build
npm run lint
```

In production `VITE_API_BASE_URL=https://api.tgbot.nikichxp.xyz` is set at image build time (see `.github/workflows/dashboard.yml`).
