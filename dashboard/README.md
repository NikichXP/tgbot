# tg-bot dashboard

Web admin UI for tg-bot: manage bots and their features. Only the Telegram user `APP_ADMIN_ID` can log in.

Stack: Vite + React + TypeScript, MUI (components), TanStack Query (server state).

## Auth

- **Login**: [Telegram Login Widget](https://core.telegram.org/widgets/login) → `POST /admin/auth/telegram`;
  the backend verifies the widget signature with the login bot's token and checks the user is the admin.
- **Access token** (15 min): in `sessionStorage`, sent as `Authorization: Bearer …`; stored in Redis on the backend.
- **Refresh token** (no expiry, revoked on logout): HttpOnly cookie on `/admin/auth`; its hash is stored in
  MongoDB (`dashboardSessions`). On a 401 the client calls `/admin/auth/refresh` once and retries.

The widget only works on the domain registered for the login bot via @BotFather → `/setdomain`
(`dashboard.tgbot.nikichxp.xyz`), so login can't be done from `localhost`.

## Development

```bash
npm install
npm run dev     # http://localhost:5173, /admin is proxied to the backend on :8080
npm run build
npm run lint
```

In production `VITE_API_BASE_URL=https://api.tgbot.nikichxp.xyz` is set at image build time (see `.github/workflows/dashboard.yml`).
