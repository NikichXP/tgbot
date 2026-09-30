import TelegramIcon from '@mui/icons-material/Telegram'
import { Alert, Box, Button, CircularProgress, Paper, Stack, Typography } from '@mui/material'
import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { authApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { loadTelegramLogin, openTelegramLogin } from '../auth/telegramLogin'

export function LoginPage() {
  const { loginWithTelegram } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [loggingIn, setLoggingIn] = useState(false)

  const config = useQuery({
    queryKey: ['auth-config'],
    queryFn: () => authApi<{ clientId: string }>('/admin/auth/config'),
  })
  const script = useQuery({ queryKey: ['telegram-login-script'], queryFn: () => loadTelegramLogin().then(() => true) })
  // Fetched in advance: the popup has to open synchronously on click. One nonce per attempt.
  const nonce = useQuery({
    queryKey: ['login-nonce'],
    queryFn: () => authApi<{ nonce: string }>('/admin/auth/nonce', { method: 'POST' }),
    staleTime: 5 * 60 * 1000,
    refetchInterval: 5 * 60 * 1000,
  })

  const login = () => {
    if (!config.data || !nonce.data) return
    setError(null)
    openTelegramLogin(config.data.clientId, nonce.data.nonce, async (result) => {
      nonce.refetch()
      if (result.error) {
        if (result.error !== 'popup_closed') setError(`Telegram: ${result.error}`)
        return
      }
      if (!result.id_token) return
      setLoggingIn(true)
      try {
        await loginWithTelegram(result.id_token)
      } catch (e) {
        setError((e as Error).message)
      } finally {
        setLoggingIn(false)
      }
    })
  }

  const loadError = config.error ?? script.error ?? nonce.error
  const ready = config.data && script.data && nonce.data && !loggingIn

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 2 }}>
      <Paper sx={{ p: 4, width: '100%', maxWidth: 400 }}>
        <Stack spacing={3} sx={{ alignItems: 'center' }}>
          <Typography variant="h5">TG Bot Dashboard</Typography>
          <Typography color="text.secondary" sx={{ textAlign: 'center' }}>
            Войдите через Telegram. Доступ есть только у администратора.
          </Typography>
          {loadError ? (
            <Alert severity="error">Не удалось подготовить вход: {loadError.message}</Alert>
          ) : (
            <Button
              variant="contained"
              size="large"
              startIcon={ready ? <TelegramIcon /> : <CircularProgress size={20} color="inherit" />}
              disabled={!ready}
              onClick={login}
              sx={{ bgcolor: '#2AABEE', '&:hover': { bgcolor: '#229ED9' } }}
            >
              Войти через Telegram
            </Button>
          )}
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </Paper>
    </Box>
  )
}
