import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiWithTokenRefresh } from './client'
import type { Bot, CreateBotRequest, Feature } from './types'

export function useBots() {
  return useQuery({ queryKey: ['bots'], queryFn: () => apiWithTokenRefresh<Bot[]>('/admin/bots') })
}

export function useFeatures() {
  return useQuery({ queryKey: ['features'], queryFn: () => apiWithTokenRefresh<Feature[]>('/admin/features') })
}

export function useCreateBot() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateBotRequest) =>
      apiWithTokenRefresh<Bot>('/admin/bots', { method: 'POST', body: JSON.stringify(request) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bots'] }),
  })
}

export function useUpdateBotFeatures() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ name, supportedFeatures }: { name: string; supportedFeatures: string[] }) =>
      apiWithTokenRefresh<Bot>(`/admin/bots/${encodeURIComponent(name)}/features`, {
        method: 'PUT',
        body: JSON.stringify({ supportedFeatures }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bots'] }),
  })
}
