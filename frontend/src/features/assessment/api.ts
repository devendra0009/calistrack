import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '@/shared/api/client'
import type {
  AssessmentResponse,
  GoalPathAssessmentResponse,
  SubmitAssessmentRequest,
} from '@/shared/api/types'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { uploadAssessmentVideo } from '@/features/media/api'

export const assessmentPathQueryKey = ['assessments', 'path'] as const

export function fetchAssessmentPath() {
  return api.get<GoalPathAssessmentResponse>('/api/v1/assessments/path')
}

export function useAssessmentPath(enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: assessmentPathQueryKey,
    queryFn: fetchAssessmentPath,
    enabled: enabled && isReady && isAuthenticated,
    retry: false,
  })
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
