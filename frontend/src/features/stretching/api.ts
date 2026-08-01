import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type {
  CurrentWorkoutSessionResponse,
  StretchingTodayResponse,
} from '@/shared/api/types'
import { currentSessionQueryKey, workoutSessionsQueryKey } from '@/features/home/api'

export const stretchingTodayQueryKey = ['stretching', 'today'] as const

export function useStretchingToday() {
  return useQuery({
    queryKey: stretchingTodayQueryKey,
    queryFn: () => api.get<StretchingTodayResponse>('/api/v1/stretching/today'),
  })
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
