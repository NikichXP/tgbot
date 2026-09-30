import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { apiWithTokenRefresh, authApi, getAccessToken, refreshAccessToken, setAccessToken, setOnSessionLost } from '../api/client'
import type { DashboardUser, LoginResponse } from '../api/types'

type AuthState =
  | { status: 'loading' }
  | { status: 'anonymous' }
  | { status: 'authenticated'; user: DashboardUser }

interface AuthContextValue {
  state: AuthState
  loginWithTelegram: (idToken: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({ status: 'loading' })

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      const hasToken = getAccessToken() !== null || (await refreshAccessToken())
      if (!hasToken) {
        if (!cancelled) setState({ status: 'anonymous' })
        return
      }
      try {
        const user = await apiWithTokenRefresh<DashboardUser>('/admin/me')
        if (!cancelled) setState({ status: 'authenticated', user })
      } catch {
        if (!cancelled) setState({ status: 'anonymous' })
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    setOnSessionLost(() => {
      setAccessToken(null)
      setState({ status: 'anonymous' })
    })
  }, [])

  const loginWithTelegram = useCallback(async (idToken: string) => {
    const response = await authApi<LoginResponse>('/admin/auth/telegram', {
      method: 'POST',
      body: JSON.stringify({ idToken }),
    })
    setAccessToken(response.accessToken)
    setState({ status: 'authenticated', user: response.user })
  }, [])

  const logout = useCallback(async () => {
    try {
      await authApi<void>('/admin/auth/logout', { method: 'POST' })
    } finally {
      setAccessToken(null)
      setState({ status: 'anonymous' })
    }
  }, [])

  const value = useMemo(() => ({ state, loginWithTelegram, logout }), [state, loginWithTelegram, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// oxlint-disable-next-line react/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
