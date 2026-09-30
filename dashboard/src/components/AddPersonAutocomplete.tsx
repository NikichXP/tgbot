import { Autocomplete, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import { useKnownUsers } from '../api/people'
import type { KnownUser } from '../api/types'
import { displayName } from '../format'
import { useDebouncedValue } from '../hooks/useDebouncedValue'

type Option = KnownUser | number

const isNumericId = (text: string) => /^-?\d+$/.test(text.trim())

interface Props {
  excludedIds: number[]
  onAdd: (userId: number) => void
  disabled?: boolean
}

export function AddPersonAutocomplete({ excludedIds, onAdd, disabled }: Props) {
  const [input, setInput] = useState('')
  const people = useKnownUsers(useDebouncedValue(input, 300))
  const found = (people.data ?? []).filter((user) => !excludedIds.includes(user.id))
  const typedId = isNumericId(input) ? Number(input.trim()) : null
  const options: Option[] =
    typedId !== null && !found.some((user) => user.id === typedId) && !excludedIds.includes(typedId)
      ? [...found, typedId]
      : found

  return (
    <Autocomplete<Option>
      options={options}
      value={null}
      inputValue={input}
      onInputChange={(_, value, reason) => reason !== 'reset' && setInput(value)}
      onChange={(_, option) => {
        if (option === null) return
        onAdd(typeof option === 'number' ? option : option.id)
        setInput('')
      }}
      filterOptions={(all) => all}
      getOptionLabel={(option) => (typeof option === 'number' ? String(option) : displayName(option.fullName, option.id))}
      isOptionEqualToValue={(a, b) => (typeof a === 'number' ? a : a.id) === (typeof b === 'number' ? b : b.id)}
      loading={people.isFetching}
      disabled={disabled}
      noOptionsText="Никого не найдено — можно ввести числовой id"
      renderOption={({ key, ...props }, option) => (
        <li key={key} {...props}>
          {typeof option === 'number' ? (
            <Typography>Добавить по id {option}</Typography>
          ) : (
            <Stack>
              <Typography>{displayName(option.fullName, option.id)}</Typography>
              <Typography variant="caption" color="text.secondary">
                {option.username ? `@${option.username} · ` : ''}id {option.id}
              </Typography>
            </Stack>
          )}
        </li>
      )}
      renderInput={(params) => (
        <TextField
          {...params}
          label="Добавить человека"
          placeholder="Имя, @username или id"
          slotProps={{
            ...params.slotProps,
            input: {
              ...params.slotProps.input,
              endAdornment: (
                <>
                  {people.isFetching && <CircularProgress size={18} />}
                  {params.slotProps.input.endAdornment}
                </>
              ),
            },
          }}
        />
      )}
    />
  )
}
