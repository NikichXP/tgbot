import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import DeleteIcon from '@mui/icons-material/DeleteOutlined'
import PersonRemoveIcon from '@mui/icons-material/PersonRemove'
import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  List,
  ListItem,
  Paper,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  useAddPeopleListMember,
  useDeletePeopleList,
  usePeopleList,
  useRemovePeopleListMember,
  useUpdatePeopleListDescription,
} from '../api/people'
import type { PeopleList } from '../api/types'
import { AddPersonAutocomplete } from '../components/AddPersonAutocomplete'
import { PersonName } from '../components/PersonName'

function DescriptionEditor({ list }: { list: PeopleList }) {
  const [draft, setDraft] = useState(list.description ?? '')
  const update = useUpdatePeopleListDescription(list.name)
  const dirty = draft.trim() !== (list.description ?? '')

  return (
    <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start' }}>
      <TextField
        label="Описание"
        fullWidth
        multiline
        value={draft}
        onChange={(e) => setDraft(e.target.value)}
        error={update.isError}
        helperText={update.isError ? update.error.message : undefined}
      />
      <Button variant="outlined" disabled={!dirty || update.isPending} onClick={() => update.mutate(draft)} sx={{ mt: 1 }}>
        Сохранить
      </Button>
    </Stack>
  )
}

function DeleteListButton({ name }: { name: string }) {
  const [confirming, setConfirming] = useState(false)
  const remove = useDeletePeopleList(name)
  const navigate = useNavigate()

  return (
    <>
      <Button color="error" startIcon={<DeleteIcon />} onClick={() => setConfirming(true)}>
        Удалить список
      </Button>
      <Dialog open={confirming} onClose={() => setConfirming(false)}>
        <DialogTitle>Удалить список «{name}»?</DialogTitle>
        <DialogContent>
          <Typography>Код, который использует этот список, перестанет находить в нём людей.</Typography>
          {remove.isError && <Alert severity="error" sx={{ mt: 2 }}>{remove.error.message}</Alert>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirming(false)}>Отмена</Button>
          <Button
            color="error"
            variant="contained"
            disabled={remove.isPending}
            onClick={() => remove.mutate(undefined, { onSuccess: () => navigate('/lists') })}
          >
            Удалить
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

function Members({ list }: { list: PeopleList }) {
  const add = useAddPeopleListMember(list.name)
  const remove = useRemovePeopleListMember(list.name)
  const error = add.error ?? remove.error

  return (
    <Stack spacing={2}>
      <Typography variant="h6">Участники ({list.members.length})</Typography>
      <AddPersonAutocomplete
        excludedIds={list.members.map((member) => member.id)}
        onAdd={(userId) => add.mutate(userId)}
        disabled={add.isPending}
      />
      {error && <Alert severity="error">{error.message}</Alert>}
      {list.members.length === 0 ? (
        <Typography color="text.secondary">В списке пока никого нет.</Typography>
      ) : (
        <Paper variant="outlined">
          <List disablePadding>
            {list.members.map((member, index) => (
              <ListItem
                key={member.id}
                divider={index < list.members.length - 1}
                secondaryAction={
                  <Tooltip title="Убрать из списка">
                    <IconButton edge="end" disabled={remove.isPending} onClick={() => remove.mutate(member.id)}>
                      <PersonRemoveIcon />
                    </IconButton>
                  </Tooltip>
                }
              >
                <PersonName id={member.id} user={member.user} />
              </ListItem>
            ))}
          </List>
        </Paper>
      )}
    </Stack>
  )
}

export function PeopleListPage() {
  const { name = '' } = useParams()
  const list = usePeopleList(name)

  return (
    <Stack spacing={3}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <IconButton component={Link} to="/lists">
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h5" sx={{ flexGrow: 1, fontFamily: 'monospace', wordBreak: 'break-all' }}>
          {name}
        </Typography>
        {list.data && <DeleteListButton name={name} />}
      </Stack>
      {list.isPending && <CircularProgress />}
      {list.isError && <Alert severity="error">{list.error.message}</Alert>}
      {list.data && (
        <>
          <DescriptionEditor key={list.data.description ?? ''} list={list.data} />
          <Members list={list.data} />
        </>
      )}
    </Stack>
  )
}
