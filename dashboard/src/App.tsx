import { Box, CircularProgress } from '@mui/material'
import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { BotsPage } from './pages/BotsPage'
import { LoginPage } from './pages/LoginPage'

export default function App() {
  const { state } = useAuth()

  if (state.status === 'loading') {
    return (
      <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (state.status === 'anonymous') return <LoginPage />

  return (
    <Layout user={state.user}>
      <Routes>
        <Route path="/bots" element={<BotsPage />} />
        <Route path="*" element={<Navigate to="/bots" replace />} />
      </Routes>
    </Layout>
  )
}
