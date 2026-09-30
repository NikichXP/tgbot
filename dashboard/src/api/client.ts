const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''
const ACCESS_TOKEN_KEY = 'tgbot.accessToken'

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

export function getAccessToken(): string | null {
  try {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY)
  } catch {
    return null
  }
}

export function setAccessToken(token: string | null) {
  try {
    if (token) sessionStorage.setItem(ACCESS_TOKEN_KEY, token)
    else sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  } catch {
    return
  }
}

async function send(path: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers)
  const token = getAccessToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  return fetch(`${API_BASE}${path}`, { ...init, headers, credentials: 'include' })
}

async function toError(response: Response): Promise<ApiError> {
  let message = `${response.status} ${response.statusText}`
  try {
    const body = await response.json()
    if (body?.error) message = body.error
  } catch {
    return new ApiError(response.status, message)
  }
  return new ApiError(response.status, message)
}

let sharedRefreshInFlight: Promise<boolean> | null = null

export function refreshAccessToken(): Promise<boolean> {
  sharedRefreshInFlight ??= (async () => {
    try {
      const response = await fetch(`${API_BASE}/admin/auth/refresh`, { method: 'POST', credentials: 'include' })
      if (!response.ok) {
        setAccessToken(null)
        return false
      }
      const body: { accessToken: string } = await response.json()
      setAccessToken(body.accessToken)
      return true
    } catch {
      return false
    } finally {
      sharedRefreshInFlight = null
    }
  })()
  return sharedRefreshInFlight
}

let onSessionLost: () => void = () => {}

export function setOnSessionLost(handler: () => void) {
  onSessionLost = handler
}

async function sendWithTokenRefresh(path: string, init: RequestInit): Promise<Response> {
  let response = await send(path, init)
  if (response.status === 401) {
    if (await refreshAccessToken()) {
      response = await send(path, init)
    }
    if (response.status === 401) onSessionLost()
  }
  return response
}

export async function apiWithTokenRefresh<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await sendWithTokenRefresh(path, init)
  if (!response.ok) throw await toError(response)
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export async function blobOrNullIfNoContentWithTokenRefresh(path: string): Promise<Blob | null> {
  const response = await sendWithTokenRefresh(path, {})
  if (response.status === 204) return null
  if (!response.ok) throw await toError(response)
  return response.blob()
}

export async function authApi<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await send(path, init)
  if (!response.ok) throw await toError(response)
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
