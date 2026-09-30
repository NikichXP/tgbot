import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useMemo } from 'react'
import { apiWithTokenRefresh, blobOrNullIfNoContentWithTokenRefresh } from './client'
import type { KnownUser, PeopleList, PeopleListSummary } from './types'

const listPath = (name: string) => `/admin/people-lists/${encodeURIComponent(name)}`

export function useKnownUsers(query: string) {
  return useQuery({
    queryKey: ['people', query],
    queryFn: () => apiWithTokenRefresh<KnownUser[]>(`/admin/people?query=${encodeURIComponent(query)}&limit=100`),
    placeholderData: (previous) => previous,
  })
}

export function usePeopleLists() {
  return useQuery({ queryKey: ['people-lists'], queryFn: () => apiWithTokenRefresh<PeopleListSummary[]>('/admin/people-lists') })
}

export function usePeopleList(name: string) {
  return useQuery({ queryKey: ['people-list', name], queryFn: () => apiWithTokenRefresh<PeopleList>(listPath(name)) })
}

function usePeopleListMutation<V>(request: (variables: V) => Promise<PeopleList | void>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: request,
    onSuccess: (list) => {
      if (list) queryClient.setQueryData(['people-list', list.name], list)
      return queryClient.invalidateQueries({ queryKey: ['people-lists'] })
    },
  })
}

export function useCreatePeopleList() {
  return usePeopleListMutation(({ name, description }: { name: string; description?: string }) =>
    apiWithTokenRefresh<PeopleList>('/admin/people-lists', { method: 'POST', body: JSON.stringify({ name, description }) }),
  )
}

export function useUpdatePeopleListDescription(name: string) {
  return usePeopleListMutation((description: string) =>
    apiWithTokenRefresh<PeopleList>(listPath(name), { method: 'PUT', body: JSON.stringify({ description }) }),
  )
}

export function useDeletePeopleList(name: string) {
  return usePeopleListMutation(() => apiWithTokenRefresh<void>(listPath(name), { method: 'DELETE' }))
}

export function useAddPeopleListMember(name: string) {
  return usePeopleListMutation((userId: number) =>
    apiWithTokenRefresh<PeopleList>(`${listPath(name)}/members/${userId}`, { method: 'PUT' }),
  )
}

export function useRemovePeopleListMember(name: string) {
  return usePeopleListMutation((userId: number) =>
    apiWithTokenRefresh<PeopleList>(`${listPath(name)}/members/${userId}`, { method: 'DELETE' }),
  )
}

export function useAvatarUrl(userId: number, enabled: boolean): string | null | undefined {
  const avatar = useQuery({
    queryKey: ['avatar', userId],
    queryFn: () => blobOrNullIfNoContentWithTokenRefresh(`/admin/people/${userId}/avatar`),
    enabled,
    staleTime: Infinity,
  })
  const url = useMemo(() => (avatar.data ? URL.createObjectURL(avatar.data) : avatar.data), [avatar.data])

  useEffect(() => () => {
    if (url) URL.revokeObjectURL(url)
  }, [url])

  return url
}
