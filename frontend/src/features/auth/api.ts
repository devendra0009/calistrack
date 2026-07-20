import { api, applyAuthResponse, clearAuth } from '@/shared/api/client'
import type { AuthResponse } from '@/shared/api/types'
import { readStoredSession } from '@/shared/lib/session-storage'

export async function register(input: {
  email: string
  password: string
  displayName: string
}): Promise<AuthResponse> {
  const auth = await api.post<AuthResponse>('/api/v1/auth/register', input, {
    auth: false,
  })
  applyAuthResponse(auth)
  return auth
}

export async function login(input: {
  email: string
  password: string
}): Promise<AuthResponse> {
  const auth = await api.post<AuthResponse>('/api/v1/auth/login', input, {
    auth: false,
  })
  applyAuthResponse(auth)
  return auth
}

export async function logout(): Promise<void> {
  const { refreshToken } = readStoredSession()
  try {
    if (refreshToken) {
      await api.post('/api/v1/auth/logout', { refreshToken }, { auth: false })
    }
  } finally {
    clearAuth()
  }
}
