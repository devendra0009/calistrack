import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type {
  CurrentWorkoutSessionResponse,
  ExerciseAttemptResponse,
  WorkoutSessionDetailResponse,
} from '@/shared/api/types'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { ApiError } from '@/shared/api/errors'

export const currentSessionQueryKey = ['workout-sessions', 'current'] as const
export const workoutSessionsQueryKey = ['workout-sessions', 'list'] as const

export function sessionDetailQueryKey(sessionId: string) {
  return ['workout-sessions', sessionId] as const
}

export function fetchCurrentWorkoutSession() {
  return api.get<CurrentWorkoutSessionResponse>(
    '/api/v1/workout-sessions/current',
  )
}

export function useCurrentWorkoutSession(enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: currentSessionQueryKey,
    queryFn: fetchCurrentWorkoutSession,
    enabled: enabled && isReady && isAuthenticated,
    retry: false,
  })
}

export function fetchWorkoutSessions() {
  return api.get<CurrentWorkoutSessionResponse[]>(
    '/api/v1/workout-sessions',
  )
}

export function useWorkoutSessions(enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: workoutSessionsQueryKey,
    queryFn: fetchWorkoutSessions,
    enabled: enabled && isReady && isAuthenticated,
    retry: false,
  })
}

export function fetchWorkoutSessionDetail(sessionId: string) {
  return api.get<WorkoutSessionDetailResponse>(
    `/api/v1/workout-sessions/${sessionId}`,
  )
}

export function useWorkoutSessionDetail(sessionId: string | undefined) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: sessionDetailQueryKey(sessionId ?? ''),
    queryFn: () => fetchWorkoutSessionDetail(sessionId!),
    enabled: Boolean(sessionId) && isReady && isAuthenticated,
    retry: false,
  })
}

export function beginWorkoutSession(sessionId: string) {
  return api.post<CurrentWorkoutSessionResponse>(
    `/api/v1/workout-sessions/${sessionId}/begin`,
  )
}

export function markExerciseCompleted(
  sessionId: string,
  workoutExerciseId: string,
) {
  return api.post<ExerciseAttemptResponse>(
    `/api/v1/workout-sessions/${sessionId}/exercises/${workoutExerciseId}/complete`,
  )
}

export function completeWorkoutSession(sessionId: string) {
  return api.post<CurrentWorkoutSessionResponse>(
    `/api/v1/workout-sessions/${sessionId}/complete`,
  )
}

export function useSessionTrainMutations(sessionId: string) {
  const qc = useQueryClient()

  const invalidate = async () => {
    await Promise.all([
      qc.invalidateQueries({ queryKey: sessionDetailQueryKey(sessionId) }),
      qc.invalidateQueries({ queryKey: currentSessionQueryKey }),
      qc.invalidateQueries({ queryKey: workoutSessionsQueryKey }),
      qc.invalidateQueries({ queryKey: ['stretching', 'today'] }),
    ])
  }

  const begin = useMutation({
    mutationFn: () => beginWorkoutSession(sessionId),
    onSuccess: invalidate,
  })

  const markDone = useMutation({
    mutationFn: (workoutExerciseId: string) =>
      markExerciseCompleted(sessionId, workoutExerciseId),
    onSuccess: invalidate,
  })

  const complete = useMutation({
    mutationFn: () => completeWorkoutSession(sessionId),
    onSuccess: invalidate,
  })

  return { begin, markDone, complete }
}

export function isSessionNotFound(error: unknown): boolean {
  return error instanceof ApiError && error.status === 404
}
