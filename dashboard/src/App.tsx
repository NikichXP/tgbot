import { Box, CircularProgress } from '@mui/material'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { BotsPage } from './pages/BotsPage'
import { LoginPage } from './pages/LoginPage'
import { OAuthClientsPage } from './pages/OAuthClientsPage'

const TELEGRAM_REGISTERED_REDIRECT_PATH = '/'

export default function App() {
  const { state } = useAuth()
  const location = useLocation()

  if (state.status === 'loading') {
    return (
      <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (state.status === 'anonymous') {
    if (location.pathname !== TELEGRAM_REGISTERED_REDIRECT_PATH) {
      return <Navigate to={TELEGRAM_REGISTERED_REDIRECT_PATH} replace />
    }
    return <LoginPage />
  }

  return (
    <Layout user={state.user}>
      <Routes>
        <Route path="/bots" element={<BotsPage />} />
        <Route path="/oauth-clients" element={<OAuthClientsPage />} />
        <Route path="*" element={<Navigate to="/bots" replace />} />
      </Routes>
    </Layout>
  )
}
