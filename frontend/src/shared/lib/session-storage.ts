const ID_TOKEN_KEY = 'calistrack.idToken'
const REFRESH_TOKEN_KEY = 'calistrack.refreshToken'
const EXPIRES_AT_KEY = 'calistrack.expiresAt'

export interface StoredSession {
  idToken: string | null
  refreshToken: string | null
  expiresAt: number | null
}

export function readStoredSession(): StoredSession {
  return {
    idToken: sessionStorage.getItem(ID_TOKEN_KEY),
    refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
    expiresAt: Number(sessionStorage.getItem(EXPIRES_AT_KEY)) || null,
  }
}

export function writeStoredSession(params: {
  idToken: string
  refreshToken: string
  expiresIn: string
}): void {
  const expiresInSec = Number(params.expiresIn) || 3600
  const expiresAt = Date.now() + expiresInSec * 1000
  sessionStorage.setItem(ID_TOKEN_KEY, params.idToken)
  sessionStorage.setItem(EXPIRES_AT_KEY, String(expiresAt))
  localStorage.setItem(REFRESH_TOKEN_KEY, params.refreshToken)
}

export function clearStoredSession(): void {
  sessionStorage.removeItem(ID_TOKEN_KEY)
  sessionStorage.removeItem(EXPIRES_AT_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}
