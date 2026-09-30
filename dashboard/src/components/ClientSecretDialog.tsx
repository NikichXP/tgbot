import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  InputAdornment,
  Stack,
  TextField,
  Tooltip,
} from '@mui/material'
import type { OAuthClientWithSecret } from '../api/types'

export function ClientSecretDialog({ issued, onClose }: { issued: OAuthClientWithSecret | null; onClose: () => void }) {
  const copy = (value: string) => navigator.clipboard?.writeText(value)

  return (
    <Dialog open={issued !== null} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Секрет клиента «{issued?.client.name}»</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <Alert severity="warning">Секрет показывается только один раз. Сохраните его в секретах сервиса.</Alert>
          {issued && (
            <>
              <TextField label="client_id" value={issued.client.clientId} slotProps={{ input: { readOnly: true } }} />
              <TextField
                label="client_secret"
                value={issued.clientSecret}
                slotProps={{
                  input: {
                    readOnly: true,
                    endAdornment: (
                      <InputAdornment position="end">
                        <Tooltip title="Скопировать">
                          <IconButton onClick={() => copy(issued.clientSecret)} edge="end">
                            <ContentCopyIcon />
                          </IconButton>
                        </Tooltip>
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button variant="contained" onClick={onClose}>
          Сохранил
        </Button>
      </DialogActions>
    </Dialog>
  )
}
