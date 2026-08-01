import { Link, useNavigate } from 'react-router'
import { startTransition, useEffect, useRef } from 'react'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import type {
  SessionExerciseLineDto,
  StretchExerciseLineDto,
  StretchingTodayResponse,
  WorkoutSessionDetailResponse,
} from '@/shared/api/types'
import { Button } from '@/shared/ui/Button'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { cn } from '@/shared/lib/cn'
import {
  useSessionTrainMutations,
  useWorkoutSessionDetail,
} from '@/features/home/api'
import {
  useStartStretchingSession,
  useStretchingToday,
} from '@/features/stretching/api'

function holdLabel(line: StretchExerciseLineDto | SessionExerciseLineDto): string {
  if (line.targetHoldSeconds != null) {
    if (line.targetSets != null && line.targetSets > 1) {
      return `${line.targetSets} × ${line.targetHoldSeconds}s`
    }
    return `${line.targetHoldSeconds}s`
  }
  if (line.targetReps != null) {
    return line.targetSets != null
      ? `${line.targetSets} × ${line.targetReps}`
      : `${line.targetReps} reps`
  }
  return 'As comfortable'
}

function howToSteps(description: string | null): string[] {
  if (!description?.trim()) return []
  return description
    .split(/\n+/)
    .map((line) => line.replace(/^\s*\d+[.)]\s*/, '').trim())
    .filter(Boolean)
    .slice(0, 3)
}

function PreviewList({ today }: { today: StretchingTodayResponse }) {
  return (
    <ul className="space-y-3">
      {today.exercises.map((line) => {
        const steps = howToSteps(line.exerciseDescription)
        return (
          <li
            key={line.workoutExerciseId}
            className="rounded-2xl border border-sky-200/80 bg-white/70 p-4 dark:bg-stone-50/80"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
                  {line.sequence}. {line.exerciseName}
                </p>
                <p className="mt-1 text-sm font-semibold tabular-nums text-stone-800">
                  {holdLabel(line)}
                </p>
                {line.notes ? (
                  <p className="mt-1 text-sm text-stone-600">{line.notes}</p>
                ) : null}
                {steps.length > 0 ? (
                  <ol className="mt-2 space-y-0.5 text-sm text-stone-600">
                    {steps.map((step, i) => (
                      <li key={i} className="flex gap-2">
                        <span className="w-4 shrink-0 text-stone-400">{i + 1}.</span>
                        <span>{step}</span>
                      </li>
                    ))}
                  </ol>
                ) : null}
              </div>
            </div>
          </li>
        )
      })}
    </ul>
  )
}

function ActiveStretchList({
  session,
  sessionId,
}: {
  session: WorkoutSessionDetailResponse
  sessionId: string
}) {
  const { markDone, complete } = useSessionTrainMutations(sessionId)
  const navigate = useNavigate()
  const readOnly =
    session.status === 'COMPLETED' || session.status === 'ABANDONED'
  const doneCount = session.exercises.filter(
    (e) => e.attempt?.status === 'COMPLETED',
  ).length
  const totalCount = session.exercises.length
  const allDone = totalCount > 0 && doneCount === totalCount

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm font-medium text-sky-950">
          {doneCount}/{totalCount} stretches done
        </p>
        <div className="h-2 w-28 overflow-hidden rounded-full bg-sky-100">
          <div
            className="h-full rounded-full bg-sky-600 transition-[width]"
            style={{
              width: `${totalCount === 0 ? 0 : (doneCount / totalCount) * 100}%`,
            }}
          />
        </div>
      </div>

      <ul className="space-y-3">
        {session.exercises.map((line) => {
          const done = line.attempt?.status === 'COMPLETED'
          const steps = howToSteps(line.exerciseDescription)
          return (
            <li
              key={line.workoutExerciseId}
              className={cn(
                'rounded-2xl border p-4 transition',
                done
                  ? 'border-sky-300 bg-sky-100/70'
                  : 'border-sky-200/80 bg-white/70 dark:bg-stone-50/80',
              )}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
                    {line.sequence}. {line.exerciseName}
                  </p>
                  <p className="mt-1 text-sm font-semibold tabular-nums text-stone-800">
                    {holdLabel(line)}
                  </p>
                  {line.notes ? (
                    <p className="mt-1 text-sm text-stone-600">{line.notes}</p>
                  ) : null}
                  {steps.length > 0 ? (
                    <ol className="mt-2 space-y-0.5 text-sm text-stone-600">
                      {steps.map((step, i) => (
                        <li key={i} className="flex gap-2">
                          <span className="w-4 shrink-0 text-stone-400">
                            {i + 1}.
                          </span>
                          <span>{step}</span>
                        </li>
                      ))}
                    </ol>
                  ) : null}
                </div>

                {done ? (
                  <span className="shrink-0 rounded-full bg-sky-700 px-3 py-1 text-xs font-semibold text-white">
                    Done
                  </span>
                ) : readOnly ? (
                  <span className="shrink-0 rounded-full bg-stone-200 px-3 py-1 text-xs font-semibold text-stone-700">
                    Skipped
                  </span>
                ) : (
                  <Button
                    className="shrink-0 bg-sky-700! hover:bg-sky-800!"
                    loading={
                      markDone.isPending &&
                      markDone.variables === line.workoutExerciseId
                    }
                    onClick={async () => {
                      try {
                        await markDone.mutateAsync(line.workoutExerciseId)
                        toast.success(`${line.exerciseName} done`)
                      } catch (err) {
                        toast.error(
                          err instanceof ApiError
                            ? err.message
                            : 'Could not mark stretch',
                        )
                      }
                    }}
                  >
                    Done
                  </Button>
                )}
              </div>
            </li>
          )
        })}
      </ul>

      {!readOnly ? (
        <>
          <Button
            className="w-full bg-sky-700! hover:bg-sky-800!"
            loading={complete.isPending}
            disabled={!allDone}
            onClick={async () => {
              try {
                await complete.mutateAsync()
                toast.success('Stretching complete — nice start to the day')
                startTransition(() => navigate('/home', { replace: true }))
              } catch (err) {
                toast.error(
                  err instanceof ApiError
                    ? err.message
                    : 'Could not finish stretching',
                )
              }
            }}
          >
            Finish stretching
          </Button>
          {!allDone ? (
            <p className="text-center text-xs text-stone-500">
              Mark every stretch done to finish.
            </p>
          ) : null}
        </>
      ) : (
        <section className="rounded-2xl border border-sky-200 bg-sky-50/90 p-5 text-center">
          <p className="text-sm font-medium text-sky-950">
            You finished this stretch day.
          </p>
          <p className="mt-1 text-sm text-sky-900/80">
            Tomorrow (or next time) you&apos;ll get the next day in the cycle.
          </p>
          <Button
            variant="secondary"
            className="mt-4"
            onClick={() => startTransition(() => navigate('/home'))}
          >
            Back to home
          </Button>
        </section>
      )}
    </div>
  )
}

function StretchSessionPanel({ sessionId }: { sessionId: string }) {
  const detail = useWorkoutSessionDetail(sessionId)
  const { begin } = useSessionTrainMutations(sessionId)
  const beginRequested = useRef(false)

  useEffect(() => {
    if (!detail.data) return
    if (detail.data.status !== 'PENDING') return
    if (beginRequested.current || begin.isPending) return
    beginRequested.current = true
    void begin.mutateAsync().catch((err) => {
      beginRequested.current = false
      toast.error(
        err instanceof ApiError ? err.message : 'Could not begin stretch',
      )
    })
  }, [detail.data, begin])

  if (detail.isLoading || (detail.data?.status === 'PENDING' && begin.isPending)) {
    return <Spinner label="Opening stretch…" />
  }

  if (detail.isError || !detail.data) {
    return (
      <p className="text-sm text-red-600">
        {detail.error instanceof ApiError
          ? detail.error.message
          : 'Could not load stretch session.'}
      </p>
    )
  }

  return <ActiveStretchList session={detail.data} sessionId={sessionId} />
}

export function StretchTodayPage() {
  const todayQuery = useStretchingToday()
  const start = useStartStretchingSession()

  if (todayQuery.isLoading) {
    return (
      <PageShell embedded title="Morning Stretch">
        <Spinner label="Loading today’s stretches…" />
      </PageShell>
    )
  }

  if (todayQuery.isError || !todayQuery.data) {
    return (
      <PageShell embedded title="Morning Stretch">
        <p className="text-sm text-red-600">
          {todayQuery.error instanceof ApiError
            ? todayQuery.error.message
            : 'Could not load stretching routine.'}
        </p>
        <Link
          to="/home"
          className="mt-4 inline-block text-sm font-medium text-sky-800"
        >
          Back to home
        </Link>
      </PageShell>
    )
  }

  const today = todayQuery.data
  const hasOpenSession =
    Boolean(today.sessionId) &&
    (today.sessionStatus === 'PENDING' ||
      today.sessionStatus === 'IN_PROGRESS')

  return (
    <PageShell
      embedded
      title="Morning Stretch"
      subtitle={`${today.workoutTitle} · Day ${today.dayNumber} of ${today.durationDays}`}
    >
      <div className="space-y-5">
        <section className="rounded-2xl border border-sky-200 bg-linear-to-br from-sky-50 via-sky-50/80 to-stone-50 p-5 shadow-sm sm:p-6">
          <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
            Daily mobility · not a skill workout
          </p>
          <p className="mt-2 text-sm text-sky-950/90">
            {today.workoutDescription ??
              'A short guided stretch. Finish this day to unlock the next in your cycle.'}
          </p>
          <p className="mt-3 text-sm font-medium text-sky-900">
            {today.exercises.length} stretches · Day {today.dayNumber}/
            {today.durationDays}
          </p>
        </section>

        {hasOpenSession && today.sessionId ? (
          <StretchSessionPanel sessionId={today.sessionId} />
        ) : (
          <>
            <PreviewList today={today} />
            <Button
              className="w-full bg-sky-700! hover:bg-sky-800!"
              loading={start.isPending}
              onClick={async () => {
                try {
                  await start.mutateAsync()
                  toast.success('Stretching started')
                } catch (err) {
                  toast.error(
                    err instanceof ApiError
                      ? err.message
                      : 'Could not start stretching',
                  )
                }
              }}
            >
              Start stretching
            </Button>
            <p className="text-center text-xs text-stone-500">
              Your skill workout stays separate — you can train both.
            </p>
          </>
        )}
      </div>
    </PageShell>
  )
}
