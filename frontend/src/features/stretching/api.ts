import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { useCallback } from 'react'
import { api } from '@/shared/api/client'
import type {
  CurrentWorkoutSessionResponse,
  StretchingTodayResponse,
} from '@/shared/api/types'
import { currentSessionQueryKey, workoutSessionsQueryKey } from '@/features/home/api'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'

export const stretchingTodayQueryKey = ['stretching', 'today'] as const

/** Warm today payload stays fresh so Home / Stretch revisits skip the network. */
export const STRETCHING_TODAY_STALE_MS = 60_000

export function fetchStretchingToday() {
  return api.get<StretchingTodayResponse>('/api/v1/stretching/today')
}

export function useStretchingToday() {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: stretchingTodayQueryKey,
    queryFn: fetchStretchingToday,
    enabled: isReady && isAuthenticated,
    staleTime: STRETCHING_TODAY_STALE_MS,
    placeholderData: keepPreviousData,
  })
}

/** Prefetch stretch today while on Home or hovering Stretch nav. */
export function usePrefetchStretchingToday() {
  const qc = useQueryClient()
  const { isAuthenticated, isReady } = useAuthSession()

  return useCallback(() => {
    if (!isReady || !isAuthenticated) return
    void qc.prefetchQuery({
      queryKey: stretchingTodayQueryKey,
      queryFn: fetchStretchingToday,
      staleTime: STRETCHING_TODAY_STALE_MS,
    })
  }, [qc, isAuthenticated, isReady])
}

export function useStartStretchingSession() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () =>
      api.post<CurrentWorkoutSessionResponse>('/api/v1/stretching/sessions'),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: stretchingTodayQueryKey })
      void queryClient.invalidateQueries({ queryKey: workoutSessionsQueryKey })
      void queryClient.invalidateQueries({ queryKey: currentSessionQueryKey })
    },
  })
}
