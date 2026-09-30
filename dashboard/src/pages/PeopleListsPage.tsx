import AddIcon from '@mui/icons-material/Add'
import { Alert, Box, Button, Card, CardActionArea, CardContent, CircularProgress, Stack, Typography } from '@mui/material'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { usePeopleLists } from '../api/people'
import { CreatePeopleListDialog } from '../components/CreatePeopleListDialog'
import { formatDateTime } from '../format'

const peopleCountLabel = (count: number) => {
  const lastTwo = count % 100
  const last = count % 10
  if (lastTwo >= 11 && lastTwo <= 14) return `${count} человек`
  if (last >= 2 && last <= 4) return `${count} человека`
  return `${count} человек`
}

export function PeopleListsPage() {
  const lists = usePeopleLists()
  const [creating, setCreating] = useState(false)

  return (
    <Stack spacing={3}>
      <Stack direction="row" sx={{ alignItems: 'center' }}>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>
          Списки людей
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreating(true)}>
          Создать
        </Button>
      </Stack>

      {lists.isPending && <CircularProgress />}
      {lists.isError && <Alert severity="error">{lists.error.message}</Alert>}
      {lists.data?.length === 0 && <Typography color="text.secondary">Списков пока нет.</Typography>}

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr' } }}>
        {lists.data?.map((list) => (
          <Card key={list.name} variant="outlined">
            <CardActionArea component={Link} to={`/lists/${encodeURIComponent(list.name)}`} sx={{ height: '100%' }}>
              <CardContent>
                <Typography variant="h6" sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}>
                  {list.name}
                </Typography>
                {list.description && (
                  <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                    {list.description}
                  </Typography>
                )}
                <Typography variant="body2" sx={{ mt: 1.5 }}>
                  {peopleCountLabel(list.memberCount)} · изменён {formatDateTime(list.updatedAt)}
                </Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        ))}
      </Box>

      <CreatePeopleListDialog open={creating} onClose={() => setCreating(false)} />
    </Stack>
  )
}
