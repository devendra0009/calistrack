import { Link, useNavigate } from 'react-router'
import { startTransition, useEffect, useRef, useState } from 'react'
import { toast } from '@/shared/ui/notify'
import { ApiError } from '@/shared/api/errors'
import type {
  StretchingTodayResponse,
  WorkoutSessionDetailResponse,
} from '@/shared/api/types'
import { Button } from '@/shared/ui/Button'
import { PageError } from '@/shared/ui/PageError'
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
import { HoldTimer } from '@/features/stretching/components/HoldTimer'
import {
  holdLabel,
  parseStretchGuide,
} from '@/features/stretching/lib/stretchGuide'
import { speakCue, preloadStretchSounds } from '@/features/stretching/lib/speakCue'
import { useWorkoutMusic } from '@/features/workout-music/WorkoutMusicProvider'

function StretchDemoImage({
  name,
  thumbnailUrl,
  demoVideoUrl,
  className,
}: {
  name: string
  thumbnailUrl: string | null
  demoVideoUrl?: string | null
  className?: string
}) {
  const src = thumbnailUrl?.trim() || demoVideoUrl?.trim() || null
  if (!src) {
    return (
      <div
        className={cn(
          'flex aspect-4/3 items-center justify-center rounded-2xl border border-dashed border-sky-200 bg-sky-50/60 text-center text-xs text-sky-800/70 sm:aspect-square',
          className,
        )}
      >
        How-to image
        <br />
        coming soon
      </div>
    )
  }

  return (
    <div
      className={cn(
        'overflow-hidden rounded-2xl border border-sky-200 bg-sky-50/40',
        className,
      )}
    >
      <img
        src={src}
        alt={`How to do ${name}`}
        className="aspect-4/3 h-full w-full object-contain sm:aspect-square"
        loading="lazy"
        onError={(e) => {
          const el = e.currentTarget
          el.style.display = 'none'
          const fallback = el.nextElementSibling
          if (fallback instanceof HTMLElement) fallback.hidden = false
        }}
      />
      <div
        hidden
        className="flex aspect-4/3 items-center justify-center p-3 text-center text-xs text-sky-800/70 sm:aspect-square"
      >
        Add how-to image for {name}
      </div>
    </div>
  )
}

function StretchGuideBlock({
  description,
  compact = false,
  stepsCollapsed = false,
}: {
  description: string | null
  compact?: boolean
  /** When true, how-to steps stay collapsed until the user opens them. */
  stepsCollapsed?: boolean
}) {
  const guide = parseStretchGuide(description)
  if (guide.steps.length === 0 && !guide.targets) return null

  return (
    <div className={cn('space-y-3', compact ? 'mt-2' : 'mt-4')}>
      {guide.targets ? (
        <div className="rounded-xl border border-sky-100 bg-sky-50/80 px-3 py-2.5">
          <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
            What this helps
          </p>
          {guide.muscles ? (
            <p className="mt-1 text-sm font-medium text-sky-950">
              {guide.muscles}
            </p>
          ) : null}
          {guide.benefit ? (
            <p className="mt-0.5 text-sm text-stone-600">{guide.benefit}</p>
          ) : !guide.muscles ? (
            <p className="mt-1 text-sm text-stone-700">{guide.targets}</p>
          ) : null}
        </div>
      ) : null}

      {guide.steps.length > 0 ? (
        stepsCollapsed ? (
          <details className="group rounded-xl border border-sky-100  open:bg-sky-50/40">
            <summary className="cursor-pointer list-none px-3 py-2.5 text-sm font-medium text-orange-200 marker:content-none [&::-webkit-details-marker]:hidden">
              <span className="flex items-center justify-between gap-2">
                Read form tips
                <span className="text-xs font-normal text-sky-700/70 group-open:hidden">
                  optional
                </span>
                <span className="hidden text-xs font-normal text-sky-700/70 group-open:inline">
                  hide
                </span>
              </span>
            </summary>
            <ol className="space-y-1.5 border-t border-sky-100 px-3 py-3 text-sm text-stone-700">
              {guide.steps.map((step, i) => (
                <li key={i} className="flex gap-2">
                  <span className="w-4 shrink-0 font-medium text-sky-700/70">
                    {i + 1}.
                  </span>
                  <span>{step}</span>
                </li>
              ))}
            </ol>
          </details>
        ) : (
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
              How to
            </p>
            <ol className="mt-1.5 space-y-1.5 text-sm text-stone-700">
              {guide.steps.map((step, i) => (
                <li key={i} className="flex gap-2">
                  <span className="w-4 shrink-0 font-medium text-sky-700/70">
                    {i + 1}.
                  </span>
                  <span>{step}</span>
                </li>
              ))}
            </ol>
          </div>
        )
      ) : null}
    </div>
  )
}

function PreviewList({ today }: { today: StretchingTodayResponse }) {
  return (
    <ul className="space-y-3">
      {today.exercises.map((line) => (
        <li
          key={line.workoutExerciseId}
          className="rounded-2xl border border-sky-200/80 bg-white/70 p-3 sm:p-4 dark:bg-stone-50/80"
        >
          <div className="flex gap-3">
            <StretchDemoImage
              name={line.exerciseName}
              thumbnailUrl={line.thumbnailUrl}
              demoVideoUrl={line.demoVideoUrl}
              className="w-16 shrink-0 sm:w-24 [&>img]:aspect-square"
            />
            <div className="min-w-0 flex-1">
              <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
                {line.sequence}. {line.exerciseName}
              </p>
              <p className="mt-1 text-sm font-semibold tabular-nums text-stone-800">
                {holdLabel(line)}
                {line.notes ? (
                  <span className="mt-0.5 block font-normal text-stone-500 sm:mt-0 sm:ml-2 sm:inline">
                    · {line.notes}
                  </span>
                ) : null}
              </p>
              <StretchGuideBlock
                description={line.exerciseDescription}
                compact
                stepsCollapsed
              />
            </div>
          </div>
        </li>
      ))}
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
  const { leaveWorkout } = useWorkoutMusic()
  const readOnly =
    session.status === 'COMPLETED' || session.status === 'ABANDONED'
  const doneCount = session.exercises.filter(
    (e) => e.attempt?.status === 'COMPLETED',
  ).length
  const totalCount = session.exercises.length
  const allDone = totalCount > 0 && doneCount === totalCount

  const current =
    session.exercises.find((e) => e.attempt?.status !== 'COMPLETED') ?? null
  const upcoming = session.exercises.filter(
    (e) =>
      e.attempt?.status !== 'COMPLETED' &&
      e.workoutExerciseId !== current?.workoutExerciseId,
  )
  const completed = session.exercises.filter(
    (e) => e.attempt?.status === 'COMPLETED',
  )

  const [holdDone, setHoldDone] = useState(false)
  useEffect(() => {
    setHoldDone(false)
  }, [current?.workoutExerciseId])

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

      {readOnly ? (
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
      ) : (
        <div className="space-y-2">
          <Button
            className="w-full bg-sky-700! hover:bg-sky-800!"
            loading={complete.isPending}
            disabled={!allDone}
            onClick={async () => {
              try {
                speakCue('finish')
                await complete.mutateAsync()
                leaveWorkout()
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
              Complete each stretch (timer optional) to finish the day.
            </p>
          ) : null}
        </div>
      )}

      {!readOnly && current ? (
        <section className="rounded-2xl border border-sky-300 bg-white/90 p-3 shadow-sm sm:p-5">
          <p className="text-xs font-semibold uppercase tracking-wide text-sky-800">
            Now · {current.sequence} of {totalCount}
          </p>
          <h2 className="mt-1 text-lg font-semibold tracking-tight text-sky-950 sm:text-xl">
            {current.exerciseName}
          </h2>
          <p className="mt-1 text-sm text-stone-600">
            Target {holdLabel(current)}
            {current.notes ? ` · ${current.notes}` : ''}
          </p>

          {/* Mobile: image first (visual), then full-width timer strip.
              sm+: timer left (narrow), image right. */}
          <div className="mt-3 flex flex-col gap-3 sm:mt-4 sm:flex-row sm:items-stretch">
            <StretchDemoImage
              name={current.exerciseName}
              thumbnailUrl={current.thumbnailUrl}
              demoVideoUrl={current.demoVideoUrl}
              className="order-1 min-w-0 w-full sm:order-2 sm:min-h-0 sm:flex-1"
            />
            <HoldTimer
              className="order-2 w-full shrink-0 sm:order-1 sm:w-auto sm:self-stretch"
              resetKey={current.workoutExerciseId}
              targetSeconds={current.targetHoldSeconds}
              onComplete={() => setHoldDone(true)}
            />
          </div>

          <StretchGuideBlock
            description={current.exerciseDescription}
            stepsCollapsed
          />

          <Button
            className={cn(
              'mt-4 w-full sm:mt-5',
              holdDone
                ? 'bg-sky-700! hover:bg-sky-800!'
                : 'bg-sky-600! hover:bg-sky-700!',
            )}
            loading={
              markDone.isPending &&
              markDone.variables === current.workoutExerciseId
            }
            onClick={async () => {
              try {
                speakCue(upcoming.length > 0 ? 'next' : 'done')
                await markDone.mutateAsync(current.workoutExerciseId)
                toast.success(`${current.exerciseName} done`)
              } catch (err) {
                toast.error(
                  err instanceof ApiError
                    ? err.message
                    : 'Could not mark stretch',
                )
              }
            }}
          >
            {holdDone ? 'Mark done & next' : 'Mark done'}
          </Button>
        </section>
      ) : null}

      {!readOnly && upcoming.length > 0 ? (
        <section>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-stone-500">
            Up next
          </p>
          <ul className="space-y-2">
            {upcoming.map((line) => (
              <li
                key={line.workoutExerciseId}
                className="flex items-center justify-between gap-3 rounded-xl border border-sky-100 bg-sky-50/40 px-3 py-2.5"
              >
                <span className="min-w-0 truncate text-sm font-medium text-stone-800">
                  {line.sequence}. {line.exerciseName}
                </span>
                <span className="shrink-0 text-xs tabular-nums text-stone-500">
                  {holdLabel(line)}
                </span>
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      {completed.length > 0 ? (
        <section>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-stone-500">
            Completed
          </p>
          <ul className="space-y-2">
            {completed.map((line) => (
              <li
                key={line.workoutExerciseId}
                className="flex items-center justify-between gap-3 rounded-xl border border-sky-200 bg-sky-100/60 px-3 py-2.5"
              >
                <span className="min-w-0 truncate text-sm font-medium text-sky-950">
                  {line.sequence}. {line.exerciseName}
                </span>
                <span className="shrink-0 rounded-full bg-sky-700 px-2.5 py-0.5 text-xs font-semibold text-white">
                  Done
                </span>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
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
      <PageError
        title="Stretch session stalled"
        message={
          detail.error instanceof ApiError
            ? detail.error.message
            : 'Could not load stretch session.'
        }
        onRetry={() => void detail.refetch()}
      />
    )
  }

  return <ActiveStretchList session={detail.data} sessionId={sessionId} />
}

export function StretchTodayPage() {
  const todayQuery = useStretchingToday()
  const start = useStartStretchingSession()
  const { enterWorkout } = useWorkoutMusic()

  useEffect(() => {
    preloadStretchSounds()
  }, [])

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
        <PageError
          title="Routine didn’t load"
          message={
            todayQuery.error instanceof ApiError
              ? todayQuery.error.message
              : 'Could not load stretching routine.'
          }
          onRetry={() => void todayQuery.refetch()}
        >
          <Link
            to="/home"
            className="mt-3 inline-block text-sm font-medium text-sky-800 hover:underline"
          >
            Back to home
          </Link>
        </PageError>
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
        <section className="rounded-2xl border border-sky-200 bg-linear-to-br from-sky-50 via-sky-50/80 to-stone-50 p-4 shadow-sm sm:p-6">
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
            <div className="space-y-2">
              <Button
                className="w-full bg-sky-700! hover:bg-sky-800!"
                loading={start.isPending}
                onClick={async () => {
                  try {
                    speakCue('start')
                    enterWorkout()
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
            </div>
            <PreviewList today={today} />
          </>
        )}
      </div>
    </PageShell>
  )
}
