import { ApiError, parseErrorMessage } from '@/shared/api/errors'
import type { AuthResponse } from '@/shared/api/types'
import {
  clearStoredSession,
  readStoredSession,
  writeStoredSession,
} from '@/shared/lib/session-storage'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8084'

type TokenListener = (session: {
  idToken: string | null
  refreshToken: string | null
}) => void

const listeners = new Set<TokenListener>()

let refreshPromise: Promise<boolean> | null = null

export function subscribeTokens(listener: TokenListener): () => void {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

function notifyListeners(): void {
  const { idToken, refreshToken } = readStoredSession()
  listeners.forEach((l) => l({ idToken, refreshToken }))
}

export function applyAuthResponse(auth: AuthResponse): void {
  writeStoredSession({
    idToken: auth.idToken,
    refreshToken: auth.refreshToken,
    expiresIn: auth.expiresIn,
  })
  notifyListeners()
}

export function clearAuth(): void {
  clearStoredSession()
  notifyListeners()
}

async function refreshTokens(): Promise<boolean> {
  const { refreshToken } = readStoredSession()
  if (!refreshToken) {
    clearAuth()
    return false
  }

  try {
    const res = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })

    const body = await res.json().catch(() => null)
    if (!res.ok) {
      clearAuth()
      return false
    }

    applyAuthResponse(body as AuthResponse)
    return true
  } catch {
    clearAuth()
    return false
  }
}

export async function ensureFreshToken(): Promise<boolean> {
  if (refreshPromise) return refreshPromise

  refreshPromise = refreshTokens().finally(() => {
    refreshPromise = null
  })
  return refreshPromise
}

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
  auth?: boolean
  skipRefresh?: boolean
}

export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { body, auth = true, skipRefresh = false, headers, ...rest } = options
  let { idToken, refreshToken } = readStoredSession()

  // Prefer a usable ID token; if missing but refresh exists, mint one first.
  if (auth && !idToken && refreshToken && !skipRefresh) {
    const refreshed = await ensureFreshToken()
    if (refreshed) {
      idToken = readStoredSession().idToken
    }
  }

  const reqHeaders = new Headers(headers)
  if (body !== undefined) {
    reqHeaders.set('Content-Type', 'application/json')
  }
  if (auth && idToken) {
    reqHeaders.set('Authorization', `Bearer ${idToken}`)
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...rest,
    headers: reqHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (res.status === 401 && auth && !skipRefresh) {
    const refreshed = await ensureFreshToken()
    if (refreshed) {
      return apiRequest<T>(path, { ...options, skipRefresh: true })
    }
    throw new ApiError(401, 'Session expired — please sign in again')
  }

  if (res.status === 204) {
    return undefined as T
  }

  const payload = await res.json().catch(() => null)

  if (!res.ok) {
    throw new ApiError(
      res.status,
      parseErrorMessage(payload, `Request failed (${res.status})`),
    )
  }

  return payload as T
}

export const api = {
  get: <T>(path: string, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'GET' }),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'POST', body }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'PATCH', body }),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'PUT', body }),
  delete: <T>(path: string, options?: RequestOptions) =>
    apiRequest<T>(path, { ...options, method: 'DELETE' }),
}
