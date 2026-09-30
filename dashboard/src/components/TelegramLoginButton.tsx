import { useEffect, useRef } from 'react'
import type { TelegramAuthData } from '../api/types'

declare global {
  interface Window {
    onTelegramAuth?: (user: TelegramAuthData) => void
  }
}

interface Props {
  botUsername: string
  onAuth: (user: TelegramAuthData) => void
}

/**
 * Official Telegram Login Widget (https://core.telegram.org/widgets/login).
 * Works only on the domain registered for the bot via @BotFather -> /setdomain.
 */
export function TelegramLoginButton({ botUsername, onAuth }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    window.onTelegramAuth = onAuth
    const script = document.createElement('script')
    script.src = 'https://telegram.org/js/telegram-widget.js?22'
    script.async = true
    script.setAttribute('data-telegram-login', botUsername)
    script.setAttribute('data-size', 'large')
    script.setAttribute('data-radius', '8')
    script.setAttribute('data-onauth', 'onTelegramAuth(user)')
    const container = containerRef.current
    container?.appendChild(script)
    return () => {
      container?.replaceChildren()
      delete window.onTelegramAuth
    }
  }, [botUsername, onAuth])

  return <div ref={containerRef} />
}
