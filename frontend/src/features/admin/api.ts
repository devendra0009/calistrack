import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type {
  AdminExerciseRequest,
  AdminExerciseResponse,
  AdminNodeEdgeRequest,
  AdminNodeEdgeResponse,
  AdminNodeRequest,
  AdminNodeResponse,
  AdminPathQuestionRequest,
  AdminPathQuestionResponse,
  AdminWorkoutExerciseRequest,
  AdminWorkoutExerciseResponse,
  AdminWorkoutRequest,
  AdminWorkoutResponse,
  AdminWorkoutSummaryResponse,
} from '@/shared/api/types'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'

export const adminKeys = {
  exercises: (status?: string) => ['admin', 'exercises', status ?? 'all'] as const,
  nodes: (status?: string) => ['admin', 'nodes', status ?? 'all'] as const,
  edges: (nodeId?: string) => ['admin', 'edges', nodeId ?? 'all'] as const,
  workouts: (status?: string, goalNodeId?: string) =>
    ['admin', 'workouts', status ?? 'all', goalNodeId ?? 'all'] as const,
  workout: (id: string) => ['admin', 'workout', id] as const,
  pathQuestions: (goalNodeId?: string) =>
    ['admin', 'path-questions', goalNodeId ?? 'all'] as const,
}

function useAdminEnabled() {
  const { isReady, isAuthenticated } = useAuthSession()
  return isReady && isAuthenticated
}

// ── Exercises ──────────────────────────────────────────────

export function useAdminExercises(status?: string) {
  const enabled = useAdminEnabled()
  return useQuery({
    queryKey: adminKeys.exercises(status),
    queryFn: () => {
      const q = status ? `?status=${encodeURIComponent(status)}` : ''
      return api.get<AdminExerciseResponse[]>(`/api/v1/admin/exercises${q}`)
    },
    enabled,
  })
}

export function useCreateExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: AdminExerciseRequest) =>
      api.post<AdminExerciseResponse>('/api/v1/admin/exercises', body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'exercises'] })
    },
  })
}

export function useUpdateExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: AdminExerciseRequest }) =>
      api.put<AdminExerciseResponse>(`/api/v1/admin/exercises/${id}`, body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'exercises'] })
    },
  })
}

export function useDeprecateExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      api.delete<AdminExerciseResponse>(`/api/v1/admin/exercises/${id}`),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'exercises'] })
    },
  })
}

// ── Nodes ──────────────────────────────────────────────────

export function useAdminNodes(status?: string) {
  const enabled = useAdminEnabled()
  return useQuery({
    queryKey: adminKeys.nodes(status),
    queryFn: () => {
      const q = status ? `?status=${encodeURIComponent(status)}` : ''
      return api.get<AdminNodeResponse[]>(`/api/v1/admin/nodes${q}`)
    },
    enabled,
  })
}

export function useCreateNode() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: AdminNodeRequest) =>
      api.post<AdminNodeResponse>('/api/v1/admin/nodes', body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'nodes'] })
    },
  })
}

export function useUpdateNode() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: AdminNodeRequest }) =>
      api.put<AdminNodeResponse>(`/api/v1/admin/nodes/${id}`, body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'nodes'] })
    },
  })
}

export function useDeprecateNode() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      api.delete<AdminNodeResponse>(`/api/v1/admin/nodes/${id}`),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'nodes'] })
    },
  })
}

// ── Edges ──────────────────────────────────────────────────

export function useAdminEdges(nodeId?: string) {
  const enabled = useAdminEnabled()
  return useQuery({
    queryKey: adminKeys.edges(nodeId),
    queryFn: () => {
      const q = nodeId ? `?nodeId=${encodeURIComponent(nodeId)}` : ''
      return api.get<AdminNodeEdgeResponse[]>(`/api/v1/admin/node-edges${q}`)
    },
    enabled,
  })
}

export function useCreateEdge() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: AdminNodeEdgeRequest) =>
      api.post<AdminNodeEdgeResponse>('/api/v1/admin/node-edges', body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'edges'] })
    },
  })
}

export function useDeleteEdge() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => api.delete<void>(`/api/v1/admin/node-edges/${id}`),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'edges'] })
    },
  })
}

// ── Workouts ───────────────────────────────────────────────

export function useAdminWorkouts(opts?: { status?: string; goalNodeId?: string }) {
  const status = opts?.status
  const goalNodeId = opts?.goalNodeId
  const enabled = useAdminEnabled()
  return useQuery({
    queryKey: adminKeys.workouts(status, goalNodeId),
    queryFn: () => {
      const params = new URLSearchParams()
      if (status) params.set('status', status)
      if (goalNodeId) params.set('goalNodeId', goalNodeId)
      const q = params.toString() ? `?${params}` : ''
      return api.get<AdminWorkoutSummaryResponse[]>(`/api/v1/admin/workouts${q}`)
    },
    enabled,
  })
}

export function useAdminWorkout(id: string | undefined) {
  const enabled = useAdminEnabled()
  return useQuery({
    queryKey: adminKeys.workout(id ?? ''),
    queryFn: () => api.get<AdminWorkoutResponse>(`/api/v1/admin/workouts/${id}`),
    enabled: enabled && Boolean(id),
  })
}

export function useCreateWorkout() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: AdminWorkoutRequest) =>
      api.post<AdminWorkoutResponse>('/api/v1/admin/workouts', body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'workouts'] })
    },
  })
}

export function useUpdateWorkout() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: AdminWorkoutRequest }) =>
      api.put<AdminWorkoutResponse>(`/api/v1/admin/workouts/${id}`, body),
    onSuccess: (data) => {
      void qc.invalidateQueries({ queryKey: ['admin', 'workouts'] })
      qc.setQueryData(adminKeys.workout(data.id), data)
    },
  })
}

export function useDeprecateWorkout() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      api.delete<AdminWorkoutResponse>(`/api/v1/admin/workouts/${id}`),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'workouts'] })
    },
  })
}

export function useAddWorkoutExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({
      workoutId,
      body,
    }: {
      workoutId: string
      body: AdminWorkoutExerciseRequest
    }) =>
      api.post<AdminWorkoutExerciseResponse>(
        `/api/v1/admin/workouts/${workoutId}/exercises`,
        body,
      ),
    onSuccess: (_data, vars) => {
      void qc.invalidateQueries({ queryKey: adminKeys.workout(vars.workoutId) })
      void qc.invalidateQueries({ queryKey: ['admin', 'workouts'] })
    },
  })
}

export function useUpdateWorkoutExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({
      workoutId,
      workoutExerciseId,
      body,
    }: {
      workoutId: string
      workoutExerciseId: string
      body: AdminWorkoutExerciseRequest
    }) =>
      api.put<AdminWorkoutExerciseResponse>(
        `/api/v1/admin/workouts/${workoutId}/exercises/${workoutExerciseId}`,
        body,
      ),
    onSuccess: (_data, vars) => {
      void qc.invalidateQueries({ queryKey: adminKeys.workout(vars.workoutId) })
    },
  })
}

export function useDeleteWorkoutExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({
      workoutId,
      workoutExerciseId,
    }: {
      workoutId: string
      workoutExerciseId: string
    }) =>
      api.delete<void>(
        `/api/v1/admin/workouts/${workoutId}/exercises/${workoutExerciseId}`,
      ),
    onSuccess: (_data, vars) => {
      void qc.invalidateQueries({ queryKey: adminKeys.workout(vars.workoutId) })
      void qc.invalidateQueries({ queryKey: ['admin', 'workouts'] })
    },
  })
}

// ── Path questions ─────────────────────────────────────────

export function useAdminPathQuestions(goalNodeId?: string) {
  const enabled = useAdminEnabled()
  return useQuery({
    queryKey: adminKeys.pathQuestions(goalNodeId),
    queryFn: () => {
      const q = goalNodeId ? `?goalNodeId=${encodeURIComponent(goalNodeId)}` : ''
      return api.get<AdminPathQuestionResponse[]>(`/api/v1/admin/path-questions${q}`)
    },
    enabled,
  })
}

export function useCreatePathQuestion() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: AdminPathQuestionRequest) =>
      api.post<AdminPathQuestionResponse>('/api/v1/admin/path-questions', body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'path-questions'] })
    },
  })
}

export function useUpdatePathQuestion() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: AdminPathQuestionRequest }) =>
      api.put<AdminPathQuestionResponse>(`/api/v1/admin/path-questions/${id}`, body),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'path-questions'] })
    },
  })
}

export function useDeletePathQuestion() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      api.delete<void>(`/api/v1/admin/path-questions/${id}`),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['admin', 'path-questions'] })
    },
  })
}
