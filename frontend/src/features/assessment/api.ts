import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { useCallback } from 'react'
import { api } from '@/shared/api/client'
import type {
  AssessmentResponse,
  GoalPathAssessmentResponse,
  SubmitAssessmentRequest,
} from '@/shared/api/types'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { uploadAssessmentVideo } from '@/features/media/api'

export const assessmentPathQueryKey = ['assessments', 'path'] as const

/** Warm path stays fresh for 5 minutes so revisiting Assess skips the network. */
export const ASSESSMENT_PATH_STALE_MS = 5 * 60 * 1000

export function fetchAssessmentPath() {
  return api.get<GoalPathAssessmentResponse>('/api/v1/assessments/path')
}

export function useAssessmentPath(enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: assessmentPathQueryKey,
    queryFn: fetchAssessmentPath,
    enabled: enabled && isReady && isAuthenticated,
    staleTime: ASSESSMENT_PATH_STALE_MS,
    placeholderData: keepPreviousData,
    retry: false,
  })
}

/** Prefetch assessment path while the user is still on Home / hovering Assess. */
export function usePrefetchAssessmentPath() {
  const qc = useQueryClient()
  const { isAuthenticated, isReady } = useAuthSession()

  return useCallback(() => {
    if (!isReady || !isAuthenticated) return
    void qc.prefetchQuery({
      queryKey: assessmentPathQueryKey,
      queryFn: fetchAssessmentPath,
      staleTime: ASSESSMENT_PATH_STALE_MS,
    })
  }, [qc, isAuthenticated, isReady])
}

export function submitAssessment(body: SubmitAssessmentRequest) {
  return api.post<AssessmentResponse>('/api/v1/assessments', body)
}

export function useVerifySkillMutation() {
  const qc = useQueryClient()

  return useMutation({
    mutationFn: async ({
      nodeId,
      file,
      workoutSessionId,
    }: {
      nodeId: string
      file: File
      workoutSessionId?: string
    }) => {
      const videoUrl = await uploadAssessmentVideo(file)
      return submitAssessment({ nodeId, videoUrl, workoutSessionId })
    },
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: assessmentPathQueryKey })
    },
  })
}
