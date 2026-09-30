const dateTimeFormat = new Intl.DateTimeFormat('ru-RU', { dateStyle: 'medium', timeStyle: 'short' })

export const formatDateTime = (iso: string) => dateTimeFormat.format(new Date(iso))

export const displayName = (fullName: string | undefined, id: number) => fullName?.trim() || `#${id}`
