import {
  createContext,
  use,
  useCallback,
  useEffect,
  useMemo,
  useState,
  startTransition,
  type ReactNode,
} from 'react'
import { applyAuthResponse, clearAuth, ensureFreshToken, subscribeTokens } from '@/shared/api/client'
import type { AuthResponse } from '@/shared/api/types'
import { readStoredSession } from '@/shared/lib/session-storage'

interface AuthSessionValue {
  idToken: string | null
  refreshToken: string | null
  isReady: boolean
  isAuthenticated: boolean
  setSession: (auth: AuthResponse) => void
  signOutLocal: () => void
  refresh: () => Promise<boolean>
}

const AuthSessionContext = createContext<AuthSessionValue | null>(null)

export function AuthSessionProvider({ children }: { children: ReactNode }) {
  const initial = readStoredSession()
  const [idToken, setIdToken] = useState<string | null>(initial.idToken)
  const [refreshToken, setRefreshToken] = useState<string | null>(initial.refreshToken)
  const [isReady, setIsReady] = useState(false)

  useEffect(() => {
    const unsub = subscribeTokens((session) => {
      startTransition(() => {
        setIdToken(session.idToken)
        setRefreshToken(session.refreshToken)
      })
    })

    async function boot() {
      const stored = readStoredSession()
      if (stored.refreshToken && !stored.idToken) {
        await ensureFreshToken()
      }
      setIsReady(true)
    }

    void boot()
    return unsub
  }, [])

  const setSession = useCallback((auth: AuthResponse) => {
    applyAuthResponse(auth)
  }, [])

  const signOutLocal = useCallback(() => {
    clearAuth()
  }, [])

  const refresh = useCallback(() => ensureFreshToken(), [])

  const value = useMemo<AuthSessionValue>(
    () => ({
      idToken,
      refreshToken,
      isReady,
      isAuthenticated: Boolean(idToken || refreshToken),
      setSession,
      signOutLocal,
      refresh,
    }),
    [idToken, refreshToken, isReady, setSession, signOutLocal, refresh],
  )

  return (
    <AuthSessionContext value={value}>
      {children}
    </AuthSessionContext>
  )
}

export function useAuthSession(): AuthSessionValue {
  const ctx = use(AuthSessionContext)
  if (!ctx) throw new Error('useAuthSession must be used within AuthSessionProvider')
  return ctx
}
