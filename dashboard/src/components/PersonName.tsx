import { Avatar, Box, Chip, CircularProgress, Stack, Tooltip, Typography } from '@mui/material'
import { useState } from 'react'
import { useAvatarUrl } from '../api/people'
import type { KnownUser } from '../api/types'
import { displayName, formatDateTime } from '../format'

function PersonCard({ id, user }: { id: number; user?: KnownUser }) {
  const avatarUrl = useAvatarUrl(id, true)
  return (
    <Stack spacing={1} sx={{ p: 0.5, minWidth: 200, maxWidth: 280 }}>
      <Box sx={{ display: 'grid', placeItems: 'center', minHeight: 160 }}>
        {avatarUrl === undefined && <CircularProgress size={24} />}
        {avatarUrl && <Box component="img" src={avatarUrl} alt="" sx={{ width: 160, height: 160, borderRadius: 2, objectFit: 'cover' }} />}
        {avatarUrl === null && (
          <Avatar sx={{ width: 96, height: 96, fontSize: 40 }}>{displayName(user?.fullName, id)[0]}</Avatar>
        )}
      </Box>
      <Typography variant="subtitle2">{displayName(user?.fullName, id)}</Typography>
      {user?.username && <Typography variant="body2" color="text.secondary">@{user.username}</Typography>}
      <Typography variant="caption" color="text.secondary">id {id}</Typography>
      {user ? (
        <>
          <Typography variant="caption" color="text.secondary">последняя активность: {formatDateTime(user.lastSeenAt)}</Typography>
          {user.chats.length > 0 && (
            <Typography variant="caption" color="text.secondary">чаты: {user.chats.map((chat) => chat.title).join(', ')}</Typography>
          )}
        </>
      ) : (
        <Typography variant="caption" color="text.secondary">бот ещё не видел этого пользователя</Typography>
      )}
    </Stack>
  )
}

export function PersonName({ id, user }: { id: number; user?: KnownUser }) {
  const [hovered, setHovered] = useState(false)
  return (
    <Tooltip
      title={hovered ? <PersonCard id={id} user={user} /> : ''}
      onOpen={() => setHovered(true)}
      placement="right-start"
      enterDelay={300}
      slotProps={{
        tooltip: {
          sx: { bgcolor: 'background.paper', color: 'text.primary', boxShadow: 6, border: 1, borderColor: 'divider', p: 1.5 },
        },
      }}
    >
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', cursor: 'default', minWidth: 0 }}>
        <Typography noWrap sx={{ fontWeight: 500 }}>
          {displayName(user?.fullName, id)}
        </Typography>
        {user?.username && (
          <Typography noWrap color="text.secondary" variant="body2">
            @{user.username}
          </Typography>
        )}
        {!user && <Chip size="small" variant="outlined" label="неизвестен" />}
      </Stack>
    </Tooltip>
  )
}
