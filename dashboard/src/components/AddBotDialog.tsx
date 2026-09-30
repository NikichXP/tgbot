import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Radio,
  RadioGroup,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { useCreateBot } from '../api/bots'
import type { UpdateFetchType } from '../api/types'
import { FeaturePicker } from './FeaturePicker'

interface Props {
  open: boolean
  onClose: () => void
  availableFeatures: string[]
}

export function AddBotDialog({ open, onClose, availableFeatures }: Props) {
  const [token, setToken] = useState('')
  const [name, setName] = useState('')
  const [updateFetchType, setUpdateFetchType] = useState<UpdateFetchType>('WEBHOOK')
  const [features, setFeatures] = useState<string[]>([])
  const create = useCreateBot()

  const close = () => {
    setToken('')
    setName('')
    setUpdateFetchType('WEBHOOK')
    setFeatures([])
    create.reset()
    onClose()
  }

  const submit = () =>
    create.mutate(
      { token: token.trim(), name: name.trim() || undefined, updateFetchType, supportedFeatures: features },
      { onSuccess: close },
    )

  return (
    <Dialog open={open} onClose={close} fullWidth maxWidth="sm">
      <DialogTitle>Добавить бота</DialogTitle>
      <DialogContent>
        <Stack spacing={3} sx={{ pt: 1 }}>
          <TextField
            label="Токен от @BotFather"
            type="password"
            autoComplete="off"
            required
            value={token}
            onChange={(e) => setToken(e.target.value)}
          />
          <TextField
            label="Имя (id)"
            helperText="Необязательно. По умолчанию — username бота в Telegram. Используется в URL вебхука."
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
          <RadioGroup row value={updateFetchType} onChange={(e) => setUpdateFetchType(e.target.value as UpdateFetchType)}>
            <FormControlLabel value="WEBHOOK" control={<Radio />} label="Webhook" />
            <FormControlLabel value="POLLING" control={<Radio />} label="Polling" />
          </RadioGroup>
          <Stack spacing={1}>
            <Typography variant="subtitle2">Фичи</Typography>
            <FeaturePicker available={availableFeatures} selected={features} onChange={setFeatures} />
          </Stack>
          {create.isError && <Alert severity="error">{create.error.message}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={close}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={!token.trim() || create.isPending}>
          Добавить
        </Button>
      </DialogActions>
    </Dialog>
  )
}
