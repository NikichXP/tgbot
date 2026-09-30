import AddIcon from '@mui/icons-material/Add'
import { Alert, Box, Button, CircularProgress, Stack, Typography } from '@mui/material'
import { useState } from 'react'
import { useBots, useFeatures } from '../api/bots'
import { AddBotDialog } from '../components/AddBotDialog'
import { BotCard } from '../components/BotCard'

export function BotsPage() {
  const bots = useBots()
  const features = useFeatures()
  const [adding, setAdding] = useState(false)
  const availableFeatures = features.data ?? []

  return (
    <Stack spacing={3}>
      <Stack direction="row" sx={{ alignItems: 'center' }}>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>
          Боты
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setAdding(true)}>
          Добавить
        </Button>
      </Stack>

      {(bots.isPending || features.isPending) && <CircularProgress />}
      {bots.isError && <Alert severity="error">{bots.error.message}</Alert>}
      {features.isError && <Alert severity="error">{features.error.message}</Alert>}

      {bots.data && features.data && (
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' } }}>
          {bots.data.map((bot) => (
            // key includes features so the card's draft resets when server data changes
            <BotCard key={`${bot.name}:${bot.supportedFeatures.join(',')}`} bot={bot} availableFeatures={availableFeatures} />
          ))}
        </Box>
      )}
      {bots.data?.length === 0 && <Typography color="text.secondary">Ботов пока нет.</Typography>}

      <AddBotDialog open={adding} onClose={() => setAdding(false)} availableFeatures={availableFeatures} />
    </Stack>
  )
}
