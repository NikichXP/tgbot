import type { TelegramLoginResult } from '../api/types'

const TELEGRAM_LOGIN_SCRIPT_URL = 'https://oauth.telegram.org/js/telegram-login.js?6'

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
    script.src = TELEGRAM_LOGIN_SCRIPT_URL
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

export function openTelegramLoginPopupSynchronously(clientId: string, nonce: string, callback: (result: TelegramLoginResult) => void) {
  const login = window.Telegram?.Login
  if (!login) throw new Error('Telegram Login ещё не загрузился')
  login.auth({ client_id: Number(clientId), nonce, lang: 'ru' }, callback)
}
