import SearchIcon from '@mui/icons-material/Search'
import {
  Alert,
  Chip,
  InputAdornment,
  LinearProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { useKnownUsers } from '../api/people'
import { PersonName } from '../components/PersonName'
import { formatDateTime } from '../format'
import { useDebouncedValue } from '../hooks/useDebouncedValue'

export function PeoplePage() {
  const [search, setSearch] = useState('')
  const people = useKnownUsers(useDebouncedValue(search, 300))

  return (
    <Stack spacing={3}>
      <Typography variant="h5">Люди</Typography>
      <TextField
        placeholder="Имя, @username или id"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> } }}
      />
      {people.isError && <Alert severity="error">{people.error.message}</Alert>}
      <TableContainer component={Paper} variant="outlined">
        {people.isFetching && <LinearProgress />}
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Пользователь</TableCell>
              <TableCell>ID</TableCell>
              <TableCell sx={{ display: { xs: 'none', md: 'table-cell' } }}>Боты</TableCell>
              <TableCell>Чаты</TableCell>
              <TableCell sx={{ display: { xs: 'none', sm: 'table-cell' } }}>Активность</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {people.data?.map((user) => (
              <TableRow key={user.id} hover>
                <TableCell sx={{ maxWidth: 280 }}>
                  <PersonName id={user.id} user={user} />
                </TableCell>
                <TableCell sx={{ fontFamily: 'monospace' }}>{user.id}</TableCell>
                <TableCell sx={{ display: { xs: 'none', md: 'table-cell' } }}>
                  <Stack direction="row" useFlexGap spacing={0.5} sx={{ flexWrap: 'wrap' }}>
                    {user.bots.map((bot) => <Chip key={bot} size="small" label={bot} />)}
                  </Stack>
                </TableCell>
                <TableCell>
                  <Tooltip title={user.chats.map((chat) => chat.title).join(', ')}>
                    <span>{user.chats.length}</span>
                  </Tooltip>
                </TableCell>
                <TableCell sx={{ display: { xs: 'none', sm: 'table-cell' }, whiteSpace: 'nowrap' }}>
                  {formatDateTime(user.lastSeenAt)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      {people.data?.length === 0 && <Typography color="text.secondary">Никого не найдено.</Typography>}
    </Stack>
  )
}
