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

export interface KnownUserChat {
  chatId: number
  title: string
  type: string
  lastSeenAt: string
}

export interface KnownUser {
  id: number
  username?: string
  fullName: string
  languageCode?: string
  isPremium?: boolean
  firstSeenAt: string
  lastSeenAt: string
  bots: string[]
  chats: KnownUserChat[]
}

export interface PeopleListSummary {
  name: string
  description?: string
  memberCount: number
  updatedAt: string
}

export interface PeopleListMember {
  id: number
  user?: KnownUser
}

export interface PeopleList {
  name: string
  description?: string
  members: PeopleListMember[]
  createdAt: string
  updatedAt: string
}
