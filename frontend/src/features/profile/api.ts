import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type { MeResponse, PatchMeRequest } from '@/shared/api/types'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'

export const meQueryKey = ['me'] as const

export function useMe(enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: meQueryKey,
    queryFn: () => api.get<MeResponse>('/api/v1/me'),
    enabled: enabled && isReady && isAuthenticated,
    retry: false,
  })
}

export function usePatchMe() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: PatchMeRequest) =>
      api.patch<MeResponse>('/api/v1/me', body),
    onSuccess: (data) => {
      qc.setQueryData(meQueryKey, data)
    },
  })
}

export function usePutGoal() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (goalNodeId: string) =>
      api.put<MeResponse>('/api/v1/me/goal', { goalNodeId }),
    onSuccess: (data) => {
      qc.setQueryData(meQueryKey, data)
    },
  })
}
