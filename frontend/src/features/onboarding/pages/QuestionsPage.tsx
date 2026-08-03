import { useQuery, useQueryClient } from '@tanstack/react-query'
import { startTransition, useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { useMe } from '@/features/profile/api'
import {
  fetchNextOnboardingQuestion,
  onboardingNextQuestionQueryKey,
  onboardingStatusQueryKey,
  submitOnboardingStep,
} from '@/features/onboarding/api'
import { currentSessionQueryKey } from '@/features/home/api'
import type {
  OnboardingAnswerDto,
  OnboardingAnswersResponse,
  OnboardingQuestionDto,
  OnboardingStepResponse,
  QuestionType,
} from '@/shared/api/types'
import { cn } from '@/shared/lib/cn'

export function QuestionsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const me = useMe()
  const goalNodeId = me.data?.goal?.id

  const [index, setIndex] = useState(0)
  const [answers, setAnswers] = useState<OnboardingAnswerDto[]>([])
  const [repsValue, setRepsValue] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [cachedQuestion, setCachedQuestion] =
    useState<OnboardingQuestionDto | null>(null)
  const [cachedTotal, setCachedTotal] = useState<number | null>(null)

  const questionQuery = useQuery({
    queryKey: onboardingNextQuestionQueryKey(goalNodeId, index),
    queryFn: () => fetchNextOnboardingQuestion(goalNodeId!, index),
    enabled: Boolean(goalNodeId) && !cachedQuestion,
  })

  const question =
    cachedQuestion ?? questionQuery.data?.question ?? null
  const total = cachedTotal ?? questionQuery.data?.total ?? null

  if (me.isLoading || (questionQuery.isLoading && !question)) {
    return (
      <PageShell title="Where are you now?">
        <Spinner />
      </PageShell>
    )
  }

  if (!goalNodeId) {
    return <Navigate to="/setup/goal" replace />
  }

  if ((questionQuery.isError && !question) || !question || total == null) {
    return (
      <PageShell title="Where are you now?">
        <p className="text-red-600">
          {questionQuery.error instanceof ApiError
            ? questionQuery.error.message
            : 'Could not load question.'}
        </p>
      </PageShell>
    )
  }

  async function finishPlacement(result: OnboardingStepResponse) {
    if (
      !result.goalNodeId ||
      !result.focusNodeId ||
      !result.sessionId ||
      !result.workoutId ||
      !result.workoutTitle ||
      !result.sessionStatus ||
      !result.placedNodes
    ) {
      throw new Error('Incomplete placement response')
    }

    const placement: OnboardingAnswersResponse = {
      goalNodeId: result.goalNodeId,
      focusNodeId: result.focusNodeId,
      sessionId: result.sessionId,
      workoutId: result.workoutId,
      workoutTitle: result.workoutTitle,
      sessionStatus: result.sessionStatus,
      placedNodes: result.placedNodes,
    }

    await Promise.all([
      queryClient.invalidateQueries({ queryKey: onboardingStatusQueryKey }),
      queryClient.invalidateQueries({ queryKey: currentSessionQueryKey }),
    ])
    sessionStorage.setItem(
      'calistrack.lastPlacement',
      JSON.stringify(placement),
    )
    toast.success('Placement complete')
    startTransition(() => navigate('/setup/result', { replace: true }))
  }

  async function submitAnswer(value: number | boolean) {
    if (!goalNodeId || !question || submitting) return

    const nextAnswers: OnboardingAnswerDto[] = [
      ...answers,
      {
        nodeId: question.nodeId,
        type: question.type as QuestionType,
        value,
      },
    ]

    setSubmitting(true)
    try {
      const result = await submitOnboardingStep({
        goalNodeId,
        answers: nextAnswers,
      })

      if (result.outcome === 'PLACED') {
        await finishPlacement(result)
        return
      }

      setAnswers(nextAnswers)
      setRepsValue('')
      setCachedQuestion(result.nextQuestion ?? null)
      setCachedTotal(result.total)
      setIndex(result.index)
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        await queryClient.invalidateQueries({
          queryKey: onboardingStatusQueryKey,
        })
        toast.message('Placement already completed')
        startTransition(() => navigate('/home', { replace: true }))
        return
      }
      toast.error(
        err instanceof ApiError ? err.message : 'Could not submit answer',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <PageShell
      title="Where are you now?"
      subtitle="One question at a time — honest answers place you on the path."
    >
      <div className="space-y-5 rounded-2xl border border-stone-200 bg-stone-50/90 p-6 shadow-sm">
        <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
          Question {index + 1} of {total}
        </p>

        <div
          className="h-1.5 overflow-hidden rounded-full bg-stone-200"
          role="progressbar"
          aria-valuemin={0}
          aria-valuemax={total}
          aria-valuenow={index + 1}
          aria-label={`Question ${index + 1} of ${total}`}
        >
          <div
            className="h-full rounded-full bg-emerald-700 transition-[width] duration-300"
            style={{ width: `${((index + 1) / total) * 100}%` }}
          />
        </div>

        <p className="text-lg font-medium text-stone-900">{question.prompt}</p>

        {question.type === 'REPS' ? (
          <div className="space-y-4">
            <Input
              label="Reps"
              type="number"
              min={0}
              value={repsValue}
              onChange={(e) => setRepsValue(e.target.value)}
              disabled={submitting}
            />
            <Button
              loading={submitting}
              disabled={repsValue.trim() === '' || Number.isNaN(Number(repsValue))}
              onClick={() => {
                const n = Number(repsValue)
                if (Number.isNaN(n)) {
                  toast.error('Enter a valid number of reps')
                  return
                }
                void submitAnswer(n)
              }}
            >
              Continue
            </Button>
          </div>
        ) : (
          <div className="flex gap-3">
            {[
              { label: 'Yes', value: true },
              { label: 'No', value: false },
            ].map((opt) => (
              <button
                key={String(opt.value)}
                type="button"
                disabled={submitting}
                onClick={() => void submitAnswer(opt.value)}
                className={cn(
                  'flex-1 rounded-lg border px-4 py-3 text-sm font-semibold transition',
                  'border-stone-300 text-stone-800 hover:border-emerald-600 hover:bg-emerald-50',
                  'disabled:cursor-not-allowed disabled:opacity-60',
                )}
              >
                {opt.label}
              </button>
            ))}
          </div>
        )}
      </div>
    </PageShell>
  )
}
