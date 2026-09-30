import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiWithTokenRefresh } from './client'
import type { CreateOAuthClientRequest, OAuthClient, OAuthClientWithSecret } from './types'

const OAUTH_CLIENTS_KEY = ['oauth-clients']

export function useOAuthClients() {
  return useQuery({
    queryKey: OAUTH_CLIENTS_KEY,
    queryFn: () => apiWithTokenRefresh<OAuthClient[]>('/admin/oauth-clients'),
  })
}

export function useCreateOAuthClient() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateOAuthClientRequest) =>
      apiWithTokenRefresh<OAuthClientWithSecret>('/admin/oauth-clients', {
        method: 'POST',
        body: JSON.stringify(request),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: OAUTH_CLIENTS_KEY }),
  })
}

export function useRotateOAuthClientSecret() {
  return useMutation({
    mutationFn: (clientId: string) =>
      apiWithTokenRefresh<OAuthClientWithSecret>(`/admin/oauth-clients/${encodeURIComponent(clientId)}/secret`, {
        method: 'POST',
      }),
  })
}

export function useDeleteOAuthClient() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (clientId: string) =>
      apiWithTokenRefresh<void>(`/admin/oauth-clients/${encodeURIComponent(clientId)}`, { method: 'DELETE' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: OAUTH_CLIENTS_KEY }),
  })
}
