import { Chip, Stack, Tooltip } from '@mui/material'
import CheckIcon from '@mui/icons-material/Check'
import type { Feature } from '../api/types'

interface Props {
  available: Feature[]
  selected: string[]
  onChange: (features: string[]) => void
  disabled?: boolean
}

const notDeclaredByAnyModule = (id: string): Feature => ({
  id,
  title: id,
  description: 'Не объявлена ни одним модулем приложения — можно только снять',
})

export function FeaturePicker({ available, selected, onChange, disabled }: Props) {
  const availableIds = available.map((feature) => feature.id)
  const selectedButUnknownToBackend = selected.filter((id) => !availableIds.includes(id)).map(notDeclaredByAnyModule)
  const shown = [...available, ...selectedButUnknownToBackend]
  const toggle = (id: string) => onChange(selected.includes(id) ? selected.filter((f) => f !== id) : [...selected, id])

  return (
    <Stack direction="row" useFlexGap spacing={1} sx={{ flexWrap: 'wrap' }}>
      {shown.map((feature) => {
        const active = selected.includes(feature.id)
        const unknown = !availableIds.includes(feature.id)
        return (
          <Tooltip key={feature.id} title={feature.description || feature.id}>
            <span>
              <Chip
                label={feature.title}
                icon={active ? <CheckIcon /> : undefined}
                color={unknown ? 'warning' : active ? 'primary' : 'default'}
                variant={active ? 'filled' : 'outlined'}
                onClick={() => toggle(feature.id)}
                disabled={disabled}
              />
            </span>
          </Tooltip>
        )
      })}
    </Stack>
  )
}
