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
