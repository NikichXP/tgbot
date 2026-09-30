import { Chip, Stack } from '@mui/material'
import CheckIcon from '@mui/icons-material/Check'

interface Props {
  available: string[]
  selected: string[]
  onChange: (features: string[]) => void
  disabled?: boolean
}

/** Clickable chips; features selected on the bot but unknown to the backend are still shown so they can be removed. */
export function FeaturePicker({ available, selected, onChange, disabled }: Props) {
  const all = [...available, ...selected.filter((f) => !available.includes(f))]
  const toggle = (feature: string) =>
    onChange(selected.includes(feature) ? selected.filter((f) => f !== feature) : [...selected, feature])

  return (
    <Stack direction="row" useFlexGap spacing={1} sx={{ flexWrap: 'wrap' }}>
      {all.map((feature) => {
        const active = selected.includes(feature)
        return (
          <Chip
            key={feature}
            label={feature}
            icon={active ? <CheckIcon /> : undefined}
            color={active ? 'primary' : 'default'}
            variant={active ? 'filled' : 'outlined'}
            onClick={() => toggle(feature)}
            disabled={disabled}
          />
        )
      })}
    </Stack>
  )
}
