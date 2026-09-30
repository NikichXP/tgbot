import type { TelegramLoginResult } from '../api/types'

// Official "Log In with Telegram" (OpenID Connect) library: https://core.telegram.org/bots/telegram-login
const SCRIPT_URL = 'https://oauth.telegram.org/js/telegram-login.js?6'

interface TelegramLoginOptions {
  client_id: number
  nonce?: string
  lang?: string
}

declare global {
  interface Window {
    Telegram?: {
      Login?: {
        auth: (options: TelegramLoginOptions, callback: (result: TelegramLoginResult) => void) => void
      }
    }
  }
}

let scriptPromise: Promise<void> | null = null

export function loadTelegramLogin(): Promise<void> {
  scriptPromise ??= new Promise<void>((resolve, reject) => {
    const script = document.createElement('script')
    script.src = SCRIPT_URL
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => {
      scriptPromise = null
      reject(new Error('Не удалось загрузить Telegram Login'))
    }
    document.head.appendChild(script)
  })
  return scriptPromise
}

/**
 * Opens the Telegram login popup. Must be called directly from a click handler (no awaits before it),
 * otherwise the browser blocks the popup. The id_token must then be verified by the backend.
 */
export function openTelegramLogin(clientId: string, nonce: string, callback: (result: TelegramLoginResult) => void) {
  const login = window.Telegram?.Login
  if (!login) throw new Error('Telegram Login ещё не загрузился')
  login.auth({ client_id: Number(clientId), nonce, lang: 'ru' }, callback)
}
