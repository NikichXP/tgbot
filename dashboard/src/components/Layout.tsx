import LogoutIcon from '@mui/icons-material/Logout'
import { AppBar, Avatar, Box, Button, Container, IconButton, Toolbar, Tooltip, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import type { DashboardUser } from '../api/types'
import { useAuth } from '../auth/AuthContext'

const NAV_ITEMS = [
  { to: '/bots', label: 'Боты' },
  { to: '/oauth-clients', label: 'OAuth' },
]

export function Layout({ user, children }: { user: DashboardUser; children: ReactNode }) {
  const { logout } = useAuth()
  return (
    <Box sx={{ minHeight: '100vh' }}>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <Typography variant="h6" sx={{ mr: 3 }}>
            TG Bot Dashboard
          </Typography>
          <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
            {NAV_ITEMS.map((item) => (
              <Button
                key={item.to}
                component={NavLink}
                to={item.to}
                color="inherit"
                sx={{ opacity: 0.75, '&.active': { opacity: 1, textDecoration: 'underline' } }}
              >
                {item.label}
              </Button>
            ))}
          </Box>
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
