import LogoutIcon from '@mui/icons-material/Logout'
import { AppBar, Avatar, Box, Container, IconButton, Toolbar, Tooltip, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import type { DashboardUser } from '../api/types'
import { useAuth } from '../auth/AuthContext'

export function Layout({ user, children }: { user: DashboardUser; children: ReactNode }) {
  const { logout } = useAuth()
  return (
    <Box sx={{ minHeight: '100vh' }}>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            TG Bot Dashboard
          </Typography>
          <Tooltip title={user.username ? `@${user.username}` : String(user.id)}>
            <Avatar src={user.photoUrl} sx={{ width: 32, height: 32, mr: 1 }}>
              {user.name?.[0]}
            </Avatar>
          </Tooltip>
          <Tooltip title="Выйти">
            <IconButton color="inherit" onClick={logout}>
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 3 }}>
        {children}
      </Container>
    </Box>
  )
}
