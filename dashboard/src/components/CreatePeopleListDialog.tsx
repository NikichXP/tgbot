import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField } from '@mui/material'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useCreatePeopleList } from '../api/people'

export function CreatePeopleListDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const create = useCreatePeopleList()
  const navigate = useNavigate()

  const close = () => {
    setName('')
    setDescription('')
    create.reset()
    onClose()
  }

  const submit = () =>
    create.mutate(
      { name: name.trim(), description: description.trim() || undefined },
      { onSuccess: (list) => list && navigate(`/lists/${encodeURIComponent(list.name)}`) },
    )

  return (
    <Dialog open={open} onClose={close} fullWidth maxWidth="sm">
      <DialogTitle>Новый список</DialogTitle>
      <DialogContent>
        <Stack spacing={3} sx={{ pt: 1 }}>
          <TextField
            label="Имя"
            required
            value={name}
            onChange={(e) => setName(e.target.value.toLowerCase())}
            helperText="a-z, 0-9, '_' и '-'. По этому имени список используется в боте (например, summary-trusted)."
          />
          <TextField
            label="Описание"
            multiline
            minRows={2}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          {create.isError && <Alert severity="error">{create.error.message}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={close}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={!name.trim() || create.isPending}>
          Создать
        </Button>
      </DialogActions>
    </Dialog>
  )
}
