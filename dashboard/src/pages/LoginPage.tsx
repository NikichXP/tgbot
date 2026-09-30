import { Alert, Box, CircularProgress, Paper, Stack, Typography } from '@mui/material'
import { useQuery } from '@tanstack/react-query'
import { useCallback, useState } from 'react'
import { authApi } from '../api/client'
import type { TelegramAuthData } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { TelegramLoginButton } from '../components/TelegramLoginButton'

export function LoginPage() {
  const { loginWithTelegram } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const config = useQuery({
    queryKey: ['auth-config'],
    queryFn: () => authApi<{ botUsername: string }>('/admin/auth/config'),
  })

  const onAuth = useCallback(
    (data: TelegramAuthData) => {
      setError(null)
      loginWithTelegram(data).catch((e: Error) => setError(e.message))
    },
    [loginWithTelegram],
  )

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', p: 2 }}>
      <Paper sx={{ p: 4, width: '100%', maxWidth: 400 }}>
        <Stack spacing={3} sx={{ alignItems: 'center' }}>
          <Typography variant="h5">TG Bot Dashboard</Typography>
          <Typography color="text.secondary" sx={{ textAlign: 'center' }}>
            Войдите через Telegram. Доступ есть только у администратора.
          </Typography>
          {config.isPending && <CircularProgress />}
          {config.isError && <Alert severity="error">Не удалось загрузить настройки входа: {config.error.message}</Alert>}
          {config.data && <TelegramLoginButton botUsername={config.data.botUsername} onAuth={onAuth} />}
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </Paper>
    </Box>
  )
}
