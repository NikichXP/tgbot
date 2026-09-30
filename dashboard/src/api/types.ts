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
  firstName?: string
  username?: string
  photoUrl?: string
}

/** Payload produced by the Telegram Login Widget. */
export interface TelegramAuthData {
  id: number
  first_name?: string
  last_name?: string
  username?: string
  photo_url?: string
  auth_date: number
  hash: string
}

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  user: DashboardUser
}
