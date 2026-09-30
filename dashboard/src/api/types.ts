export type UpdateFetchType = 'WEBHOOK' | 'POLLING'

export interface Bot {
  name: string
  updateFetchType: UpdateFetchType
  supportedFeatures: string[]
}

export interface CreateBotRequest {
  name?: string
  token: string
  updateFetchType: UpdateFetchType
  supportedFeatures: string[]
}

export interface DashboardUser {
  id: number
  name?: string
  username?: string
  photoUrl?: string
}

export interface TelegramLoginResult {
  id_token?: string
  error?: string
}

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  user: DashboardUser
}

export interface OAuthClient {
  clientId: string
  name: string
  redirectUris: string[]
  createdAt: string
}

export interface CreateOAuthClientRequest {
  clientId: string
  name?: string
  redirectUris: string[]
}

export interface OAuthClientWithSecret {
  client: OAuthClient
  clientSecret: string
}
