import { api } from '@/shared/api/client'
import type {
  OnboardingAnswersRequest,
  OnboardingAnswersResponse,
  OnboardingNextQuestionResponse,
  OnboardingQuestionsResponse,
  OnboardingStatusResponse,
  OnboardingStepRequest,
  OnboardingStepResponse,
} from '@/shared/api/types'
import { useQuery } from '@tanstack/react-query'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'

export const onboardingStatusQueryKey = ['onboarding', 'status'] as const

export function onboardingNextQuestionQueryKey(
  goalNodeId: string | undefined,
  index: number,
) {
  return ['onboarding', 'questions', 'next', goalNodeId, index] as const
}

export function fetchOnboardingStatus() {
  return api.get<OnboardingStatusResponse>('/api/v1/onboarding/status')
}

export function useOnboardingStatus(enabled = true) {
  const { isAuthenticated, isReady } = useAuthSession()

  return useQuery({
    queryKey: onboardingStatusQueryKey,
    queryFn: fetchOnboardingStatus,
    enabled: enabled && isReady && isAuthenticated,
    retry: false,
  })
}

export function fetchOnboardingQuestions(goalNodeId: string) {
  const qs = new URLSearchParams({ goalNodeId })
  return api.get<OnboardingQuestionsResponse>(
    `/api/v1/onboarding/questions?${qs}`,
  )
}

export function fetchNextOnboardingQuestion(goalNodeId: string, index: number) {
  const qs = new URLSearchParams({
    goalNodeId,
    index: String(index),
  })
  return api.get<OnboardingNextQuestionResponse>(
    `/api/v1/onboarding/questions/next?${qs}`,
  )
}

export function submitOnboardingAnswers(body: OnboardingAnswersRequest) {
  return api.post<OnboardingAnswersResponse>(
    '/api/v1/onboarding/answers',
    body,
  )
}

export function submitOnboardingStep(body: OnboardingStepRequest) {
  return api.post<OnboardingStepResponse>('/api/v1/onboarding/step', body)
}
