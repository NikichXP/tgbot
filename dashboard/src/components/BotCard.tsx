import { Alert, Button, Card, CardActions, CardContent, Chip, Stack, Typography } from '@mui/material'
import { useState } from 'react'
import { useUpdateBotFeatures } from '../api/bots'
import type { Bot } from '../api/types'
import { FeaturePicker } from './FeaturePicker'

const sameSet = (a: string[], b: string[]) => a.length === b.length && a.every((x) => b.includes(x))

export function BotCard({ bot, availableFeatures }: { bot: Bot; availableFeatures: string[] }) {
  const [draft, setDraft] = useState<string[]>(bot.supportedFeatures)
  const update = useUpdateBotFeatures()
  const dirty = !sameSet(draft, bot.supportedFeatures)

  const save = () =>
    update.mutate({ name: bot.name, supportedFeatures: draft }, { onSuccess: (saved) => setDraft(saved.supportedFeatures) })

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack direction="row" spacing={1} sx={{ mb: 2, alignItems: 'center' }}>
          <Typography variant="h6" sx={{ flexGrow: 1, wordBreak: 'break-all' }}>
            {bot.name}
          </Typography>
          <Chip size="small" label={bot.updateFetchType.toLowerCase()} />
        </Stack>
        <FeaturePicker
          available={availableFeatures}
          selected={draft}
          onChange={setDraft}
          disabled={update.isPending}
        />
        {update.isError && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {update.error.message}
          </Alert>
        )}
      </CardContent>
      <CardActions sx={{ justifyContent: 'flex-end' }}>
        <Button disabled={!dirty || update.isPending} onClick={() => setDraft(bot.supportedFeatures)}>
          Отменить
        </Button>
        <Button variant="contained" disabled={!dirty || update.isPending} onClick={save}>
          Сохранить
        </Button>
      </CardActions>
    </Card>
  )
}
