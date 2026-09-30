// Access token: short-lived, kept in sessionStorage and sent as `Authorization: Bearer`.
// Refresh token: long-lived, kept by the browser in an HttpOnly cookie that JS can't read;
// it is only sent to /admin/auth/*, so a new access token can be obtained after expiry or in a new tab.

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
    // storage unavailable (private mode etc.) - the refresh cookie still keeps us logged in
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
    // not JSON
  }
  return new ApiError(response.status, message)
}

// Several requests may hit 401 at once; they must share a single refresh call.
let refreshInFlight: Promise<boolean> | null = null

export function refreshAccessToken(): Promise<boolean> {
  refreshInFlight ??= (async () => {
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
      refreshInFlight = null
    }
  })()
  return refreshInFlight
}

let onSessionLost: () => void = () => {}

export function setOnSessionLost(handler: () => void) {
  onSessionLost = handler
}

/** Authenticated JSON request; transparently refreshes the access token once on 401. */
export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response = await send(path, init)
  if (response.status === 401) {
    if (await refreshAccessToken()) {
      response = await send(path, init)
    }
    if (response.status === 401) onSessionLost()
  }
  if (!response.ok) throw await toError(response)
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

/** Unauthenticated request to /admin/auth/*. */
export async function authApi<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await send(path, init)
  if (!response.ok) throw await toError(response)
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
