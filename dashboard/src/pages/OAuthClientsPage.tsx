import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import KeyIcon from '@mui/icons-material/Key'
import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { useDeleteOAuthClient, useOAuthClients, useRotateOAuthClientSecret } from '../api/oauthClients'
import type { OAuthClient, OAuthClientWithSecret } from '../api/types'
import { AddOAuthClientDialog } from '../components/AddOAuthClientDialog'
import { ClientSecretDialog } from '../components/ClientSecretDialog'

function OAuthClientCard({ client, onSecretIssued }: { client: OAuthClient; onSecretIssued: (issued: OAuthClientWithSecret) => void }) {
  const rotate = useRotateOAuthClientSecret()
  const remove = useDeleteOAuthClient()

  const rotateSecret = () => {
    if (!window.confirm(`Выпустить новый секрет для «${client.name}»? Старый сразу перестанет работать.`)) return
    rotate.mutate(client.clientId, { onSuccess: onSecretIssued })
  }

  const deleteClient = () => {
    if (!window.confirm(`Удалить клиента «${client.name}»? Вход через него перестанет работать.`)) return
    remove.mutate(client.clientId)
  }

  const error = rotate.error ?? remove.error

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6">{client.name}</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace', mb: 1 }}>
          client_id: {client.clientId}
        </Typography>
        <Typography variant="subtitle2">Redirect URI</Typography>
        {client.redirectUris.map((uri) => (
          <Typography key={uri} variant="body2" sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}>
            {uri}
          </Typography>
        ))}
        {error && (
          <Alert severity="error" sx={{ mt: 1 }}>
            {error.message}
          </Alert>
        )}
      </CardContent>
      <CardActions>
        <Button startIcon={<KeyIcon />} onClick={rotateSecret} disabled={rotate.isPending}>
          Новый секрет
        </Button>
        <Button color="error" startIcon={<DeleteIcon />} onClick={deleteClient} disabled={remove.isPending}>
          Удалить
        </Button>
      </CardActions>
    </Card>
  )
}

export function OAuthClientsPage() {
  const clients = useOAuthClients()
  const [adding, setAdding] = useState(false)
  const [issued, setIssued] = useState<OAuthClientWithSecret | null>(null)

  return (
    <Stack spacing={3}>
      <Stack direction="row" sx={{ alignItems: 'center' }}>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>
          OAuth-клиенты
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAdding(true)}>
          Добавить
        </Button>
      </Stack>
      <Typography color="text.secondary">
        Сервисы, которые используют вход через Telegram, предоставляемый tg-bot.
      </Typography>

      {clients.isPending && <CircularProgress />}
      {clients.isError && <Alert severity="error">{clients.error.message}</Alert>}

      {clients.data && (
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' } }}>
          {clients.data.map((client) => (
            <OAuthClientCard key={client.clientId} client={client} onSecretIssued={setIssued} />
          ))}
        </Box>
      )}
      {clients.data?.length === 0 && <Typography color="text.secondary">Клиентов пока нет.</Typography>}

      <AddOAuthClientDialog open={adding} onClose={() => setAdding(false)} onCreated={setIssued} />
      <ClientSecretDialog issued={issued} onClose={() => setIssued(null)} />
    </Stack>
  )
}
