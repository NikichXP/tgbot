import { Box, CircularProgress } from '@mui/material'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { BotsPage } from './pages/BotsPage'
import { LoginPage } from './pages/LoginPage'

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
    // Telegram uses the page URL as redirect_uri and only accepts pre-registered ones: keep login on "/"
    if (location.pathname !== '/') return <Navigate to="/" replace />
    return <LoginPage />
  }

  return (
    <Layout user={state.user}>
      <Routes>
        <Route path="/bots" element={<BotsPage />} />
        <Route path="*" element={<Navigate to="/bots" replace />} />
      </Routes>
    </Layout>
  )
}
