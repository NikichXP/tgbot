import LogoutIcon from '@mui/icons-material/Logout'
import { AppBar, Avatar, Box, Container, IconButton, Tab, Tabs, Toolbar, Tooltip, Typography } from '@mui/material'
import type { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'
import type { DashboardUser } from '../api/types'
import { useAuth } from '../auth/AuthContext'

const sections = [
  { path: '/bots', label: 'Боты' },
  { path: '/people', label: 'Люди' },
  { path: '/lists', label: 'Списки' },
  { path: '/oauth-clients', label: 'OAuth' },
]

export function Layout({ user, children }: { user: DashboardUser; children: ReactNode }) {
  const { logout } = useAuth()
  const { pathname } = useLocation()
  const currentSection = sections.find((section) => pathname.startsWith(section.path))?.path ?? false

  return (
    <Box sx={{ minHeight: '100vh' }}>
      <AppBar position="static" elevation={0}>
        <Toolbar sx={{ gap: 2 }}>
          <Typography variant="h6" sx={{ display: { xs: 'none', sm: 'block' }, whiteSpace: 'nowrap' }}>
            TG Bot Dashboard
          </Typography>
          <Tabs
            value={currentSection}
            textColor="inherit"
            indicatorColor="secondary"
            variant="scrollable"
            scrollButtons={false}
            sx={{ flexGrow: 1 }}
          >
            {sections.map((section) => (
              <Tab key={section.path} value={section.path} label={section.label} component={Link} to={section.path} />
            ))}
          </Tabs>
          <Tooltip title={user.username ? `@${user.username}` : String(user.id)}>
            <Avatar src={user.photoUrl} sx={{ width: 32, height: 32 }}>
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
