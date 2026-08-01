import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Controller, useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { startTransition } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { useMe } from '@/features/profile/api'
import {
  fetchOnboardingQuestions,
  onboardingStatusQueryKey,
  submitOnboardingAnswers,
} from '@/features/onboarding/api'
import { currentSessionQueryKey } from '@/features/home/api'
import type { OnboardingAnswerDto, QuestionType } from '@/shared/api/types'

type AnswerMap = Record<string, string>

export function QuestionsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const me = useMe()
  const goalNodeId = me.data?.goal?.id

  const questions = useQuery({
    queryKey: ['onboarding', 'questions', goalNodeId],
    queryFn: () => fetchOnboardingQuestions(goalNodeId!),
    enabled: Boolean(goalNodeId),
  })

  const schema = z.record(z.string(), z.string().min(1, 'Required'))

  const form = useForm<AnswerMap>({
    resolver: zodResolver(schema),
  })

  if (me.isLoading || questions.isLoading) {
    return (
      <PageShell title="Placement questions">
        <Spinner />
      </PageShell>
    )
  }

  if (!goalNodeId) {
    return <Navigate to="/setup/goal" replace />
  }

  if (questions.isError || !questions.data) {
    return (
      <PageShell title="Placement questions">
        <p className="text-red-600">
          {questions.error instanceof ApiError
            ? questions.error.message
            : 'Could not load questions.'}
        </p>
      </PageShell>
    )
  }

  const qList = questions.data.questions

  return (
    <PageShell
      title="Where are you now?"
      subtitle="Answer honestly — this places you on the path and creates your first session."
    >
      <form
        className="space-y-5 rounded-2xl border border-stone-200 bg-stone-50/90 p-6 shadow-sm"
        onSubmit={form.handleSubmit(async (values) => {
          const answers: OnboardingAnswerDto[] = qList.map((q) => {
            const raw = values[q.nodeId]
            let value: number | boolean | string = raw
            if (q.type === 'REPS') value = Number(raw)
            if (q.type === 'YES_NO') value = raw === 'true'
            return {
              nodeId: q.nodeId,
              type: q.type as QuestionType,
              value,
            }
          })

          try {
            const result = await submitOnboardingAnswers({
              goalNodeId,
              answers,
            })
            await Promise.all([
              queryClient.invalidateQueries({ queryKey: onboardingStatusQueryKey }),
              queryClient.invalidateQueries({ queryKey: currentSessionQueryKey }),
            ])
            sessionStorage.setItem(
              'calistrack.lastPlacement',
              JSON.stringify(result),
            )
            toast.success('Placement complete')
            startTransition(() => navigate('/setup/result', { replace: true }))
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
              err instanceof ApiError ? err.message : 'Could not submit answers',
            )
          }
        })}
      >
        {qList.map((q) => (
          <div
            key={q.nodeId}
            className="space-y-2 border-b border-stone-100 pb-4 last:border-0"
          >
            <p className="font-medium text-stone-900">{q.prompt}</p>
            {q.type === 'REPS' ? (
              <Input
                label="Reps"
                type="number"
                min={0}
                {...form.register(q.nodeId)}
                error={form.formState.errors[q.nodeId]?.message}
              />
            ) : (
              <Controller
                control={form.control}
                name={q.nodeId}
                render={({ field }) => (
                  <div className="flex gap-3">
                    {[
                      { label: 'Yes', value: 'true' },
                      { label: 'No', value: 'false' },
                    ].map((opt) => (
                      <button
                        key={opt.value}
                        type="button"
                        onClick={() => field.onChange(opt.value)}
                        className={
                          field.value === opt.value
                            ? 'rounded-lg bg-emerald-700 px-4 py-2 text-sm font-semibold text-white'
                            : 'rounded-lg border border-stone-300 px-4 py-2 text-sm font-semibold text-stone-800'
                        }
                      >
                        {opt.label}
                      </button>
                    ))}
                    {form.formState.errors[q.nodeId] ? (
                      <span className="text-xs text-red-600">
                        {form.formState.errors[q.nodeId]?.message}
                      </span>
                    ) : null}
                  </div>
                )}
              />
            )}
          </div>
        ))}

        <Button type="submit" loading={form.formState.isSubmitting}>
          Place me on the path
        </Button>
      </form>
    </PageShell>
  )
}
