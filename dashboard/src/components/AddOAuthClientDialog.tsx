import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from '@mui/material'
import { useState } from 'react'
import { useCreateOAuthClient } from '../api/oauthClients'
import type { OAuthClientWithSecret } from '../api/types'

interface Props {
  open: boolean
  onClose: () => void
  onCreated: (issued: OAuthClientWithSecret) => void
}

const splitLines = (text: string) =>
  text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)

export function AddOAuthClientDialog({ open, onClose, onCreated }: Props) {
  const [clientId, setClientId] = useState('')
  const [name, setName] = useState('')
  const [redirectUris, setRedirectUris] = useState('')
  const create = useCreateOAuthClient()

  const close = () => {
    setClientId('')
    setName('')
    setRedirectUris('')
    create.reset()
    onClose()
  }

  const submit = () =>
    create.mutate(
      { clientId: clientId.trim(), name: name.trim() || undefined, redirectUris: splitLines(redirectUris) },
      {
        onSuccess: (issued) => {
          close()
          onCreated(issued)
        },
      },
    )

  return (
    <Dialog open={open} onClose={close} fullWidth maxWidth="sm">
      <DialogTitle>Новый OAuth-клиент</DialogTitle>
      <DialogContent>
        <Stack spacing={3} sx={{ pt: 1 }}>
          <TextField
            label="client_id"
            required
            helperText="Строчные латинские буквы, цифры, '_' и '-', например pancakes"
            value={clientId}
            onChange={(e) => setClientId(e.target.value)}
          />
          <TextField
            label="Название"
            helperText="Показывается пользователю на странице входа"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <TextField
            label="Redirect URI"
            required
            multiline
            minRows={3}
            helperText="По одному на строку. https (http — только localhost). Их origin'ы могут встраивать виджет входа."
            value={redirectUris}
            onChange={(e) => setRedirectUris(e.target.value)}
          />
          {create.isError && <Alert severity="error">{create.error.message}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={close}>Отмена</Button>
        <Button
          variant="contained"
          onClick={submit}
          disabled={!clientId.trim() || splitLines(redirectUris).length === 0 || create.isPending}
        >
          Создать
        </Button>
      </DialogActions>
    </Dialog>
  )
}
