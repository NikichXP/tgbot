import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type { Bot, CreateBotRequest } from './types'

export function useBots() {
  return useQuery({ queryKey: ['bots'], queryFn: () => api<Bot[]>('/admin/bots') })
}

export function useFeatures() {
  return useQuery({ queryKey: ['features'], queryFn: () => api<string[]>('/admin/features') })
}

export function useCreateBot() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateBotRequest) =>
      api<Bot>('/admin/bots', { method: 'POST', body: JSON.stringify(request) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bots'] }),
  })
}

export function useUpdateBotFeatures() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ name, supportedFeatures }: { name: string; supportedFeatures: string[] }) =>
      api<Bot>(`/admin/bots/${encodeURIComponent(name)}/features`, {
        method: 'PUT',
        body: JSON.stringify({ supportedFeatures }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bots'] }),
  })
}
