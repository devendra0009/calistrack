import { useNavigate } from 'react-router'
import { startTransition, useEffect, useMemo, useState } from 'react'
import { Button } from '@/shared/ui/Button'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { cn } from '@/shared/lib/cn'
import { useMe } from '@/features/profile/api'
import { useWorkoutSessions } from '@/features/home/api'
import { useStretchingToday } from '@/features/stretching/api'
import { usePrefetchAssessmentPath } from '@/features/assessment/api'
import { useWorkoutMusic } from '@/features/workout-music/WorkoutMusicProvider'
import type { CurrentWorkoutSessionResponse } from '@/shared/api/types'

type SessionFilter = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED'

const FILTERS: {
  id: SessionFilter
  label: string
}[] = [
  { id: 'PENDING', label: 'Pending' },
  { id: 'IN_PROGRESS', label: 'In progress' },
  { id: 'COMPLETED', label: 'Completed' },
]

function isSkillSession(session: CurrentWorkoutSessionResponse): boolean {
  return session.workoutKind !== 'STRETCH'
}

function matchesFilter(
  session: CurrentWorkoutSessionResponse,
  filter: SessionFilter,
): boolean {
  switch (filter) {
    case 'PENDING':
      return session.status === 'PENDING'
    case 'IN_PROGRESS':
      return session.status === 'IN_PROGRESS'
    case 'COMPLETED':
      return session.status === 'COMPLETED'
  }
}

function emptyCopy(filter: SessionFilter): string {
  switch (filter) {
    case 'PENDING':
      return 'No pending sessions. Finish placement or complete your current path step to unlock the next one.'
    case 'IN_PROGRESS':
      return 'Nothing in progress. Start a pending session when you are ready to train.'
    case 'COMPLETED':
      return 'No completed sessions yet. Finish a workout to see it here.'
  }
}

function ctaLabel(session: CurrentWorkoutSessionResponse): string {
  if (session.status === 'PENDING') return 'Start training'
  if (session.status === 'IN_PROGRESS') return 'Continue training'
  return 'View session'
}

function formatSessionDate(iso: string): string {
  try {
    return new Intl.DateTimeFormat(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    }).format(new Date(iso))
  } catch {
    return ''
  }
}

function pickDefaultFilter(
  sessions: CurrentWorkoutSessionResponse[],
): SessionFilter {
  if (sessions.some((s) => s.status === 'IN_PROGRESS')) return 'IN_PROGRESS'
  if (sessions.some((s) => s.status === 'PENDING')) return 'PENDING'
  if (sessions.some((s) => s.status === 'COMPLETED')) return 'COMPLETED'
  return 'PENDING'
}

function MorningStretchCard() {
  const navigate = useNavigate()
  const stretch = useStretchingToday()
  const { enterWorkout } = useWorkoutMusic()

  if (stretch.isLoading) {
    return (
      <section className="rounded-2xl border border-sky-200 bg-sky-50/80 p-5 shadow-sm sm:p-6">
        <Spinner label="Loading stretch…" />
      </section>
    )
  }

  if (stretch.isError || !stretch.data) {
    return (
      <section className="rounded-2xl border border-sky-200 bg-sky-50/80 p-5 shadow-sm sm:p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-sky-800">
          Morning Stretch
        </h2>
        <p className="mt-2 text-sm text-sky-950/80">
          Could not load today&apos;s stretch. Open Stretch to retry.
        </p>
        <Button
          className="mt-4 w-full bg-sky-700! hover:bg-sky-800! sm:w-auto"
          onClick={() => startTransition(() => navigate('/stretch'))}
        >
          Open Stretch
        </Button>
      </section>
    )
  }

  const today = stretch.data
  const inProgress =
    today.sessionStatus === 'PENDING' || today.sessionStatus === 'IN_PROGRESS'

  return (
    <section className="rounded-2xl border border-sky-200 bg-linear-to-br from-sky-50 via-sky-50/90 to-stone-50 p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-sky-800">
              Morning Stretch
            </h2>
            <span className="rounded-full bg-sky-100 px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide text-sky-900">
              Mobility
            </span>
          </div>
          <p className="mt-2 text-xl font-bold text-sky-950">
            Day {today.dayNumber} of {today.durationDays}
          </p>
          <p className="mt-1 text-sm text-sky-950/80">{today.workoutTitle}</p>
          <p className="mt-2 text-sm text-stone-600">
            {today.exercises.length} stretches
            {inProgress ? ' · in progress' : ' · separate from skill training'}
          </p>
        </div>
        <Button
          className="w-full shrink-0 bg-sky-700! hover:bg-sky-800! sm:w-auto"
          onClick={() => {
            if (inProgress) enterWorkout()
            startTransition(() => navigate('/stretch'))
          }}
        >
          {inProgress ? 'Continue stretch' : 'Open stretch'}
        </Button>
      </div>
    </section>
  )
}

export function HomePage() {
  const me = useMe()
  const sessionsQuery = useWorkoutSessions(Boolean(me.data))
  const navigate = useNavigate()
  const { enterWorkout } = useWorkoutMusic()
  const prefetchAssessment = usePrefetchAssessmentPath()
  const [filter, setFilter] = useState<SessionFilter | null>(null)

  const skillSessions = useMemo(
    () => (sessionsQuery.data ?? []).filter(isSkillSession),
    [sessionsQuery.data],
  )

  const counts = useMemo(() => {
    const next: Record<SessionFilter, number> = {
      PENDING: 0,
      IN_PROGRESS: 0,
      COMPLETED: 0,
    }
    for (const session of skillSessions) {
      if (session.status === 'PENDING') next.PENDING += 1
      else if (session.status === 'IN_PROGRESS') next.IN_PROGRESS += 1
      else if (session.status === 'COMPLETED') next.COMPLETED += 1
    }
    return next
  }, [skillSessions])

  useEffect(() => {
    if (me.data) prefetchAssessment()
  }, [me.data, prefetchAssessment])

  useEffect(() => {
    if (filter !== null || !sessionsQuery.isSuccess) return
    setFilter(pickDefaultFilter(skillSessions))
  }, [filter, skillSessions, sessionsQuery.isSuccess])

  const activeFilter = filter ?? 'PENDING'
  const filtered = skillSessions.filter((s) => matchesFilter(s, activeFilter))

  if (me.isLoading || sessionsQuery.isLoading) {
    return (
      <PageShell embedded title="Home">
        <Spinner />
      </PageShell>
    )
  }

  const user = me.data

  return (
    <PageShell
      embedded
      title={`Hey, ${user?.displayName ?? 'athlete'}`}
      subtitle="Stretch to loosen up, or train your skill path."
    >
      <div className="space-y-5">
        <MorningStretchCard />

        <section className="rounded-2xl border border-stone-200 bg-stone-50/90 p-6 shadow-sm">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div className="min-w-0">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-stone-500">
                Skill goal
              </h2>
              <p className="mt-2 text-xl font-bold text-stone-900">
                {user?.goal?.name ?? 'No goal set'}
              </p>
              {user?.goal?.description ? (
                <p className="mt-1 text-sm text-stone-600">
                  {user.goal.description}
                </p>
              ) : null}
            </div>
            <Button
              variant="secondary"
              className="w-full shrink-0 sm:w-auto"
              onMouseEnter={prefetchAssessment}
              onFocus={prefetchAssessment}
              onClick={() =>
                startTransition(() => navigate('/assessment'))
              }
            >
              Verify skills
            </Button>
          </div>
        </section>

        {skillSessions.some((s) => s.awaitingVerify) ? (
          <section className="rounded-2xl border border-amber-200 bg-amber-50/90 p-5 shadow-sm sm:p-6">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-amber-800">
              Plan complete
            </h2>
            <p className="mt-2 text-sm text-amber-950">
              You finished every day for{' '}
              <span className="font-semibold">
                {skillSessions.find((s) => s.awaitingVerify)?.focusNodeName}
              </span>
              . Verify the skill to unlock the next node&apos;s Day 1.
            </p>
            <Button
              className="mt-4 w-full sm:w-auto"
              onMouseEnter={prefetchAssessment}
              onFocus={prefetchAssessment}
              onClick={() => startTransition(() => navigate('/assessment'))}
            >
              Verify skill to continue
            </Button>
          </section>
        ) : null}

        <section className="rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm sm:p-6">
          <div className="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-stone-500">
                Skill workouts
              </h2>
              <p className="mt-1 text-sm text-stone-600">
                Path training sessions — separate from morning stretch.
              </p>
            </div>
          </div>

          <div
            className="mt-4 flex w-full gap-1 overflow-x-auto rounded-xl border border-stone-200 bg-stone-100/80 p-1"
            role="tablist"
            aria-label="Session status"
          >
            {FILTERS.map((tab) => {
              const selected = activeFilter === tab.id
              return (
                <button
                  key={tab.id}
                  type="button"
                  role="tab"
                  aria-selected={selected}
                  className={cn(
                    'min-w-0 flex-1 rounded-lg px-2.5 py-2 text-center text-xs font-semibold transition sm:text-sm',
                    selected
                      ? 'bg-stone-50 text-stone-900 shadow-sm'
                      : 'text-stone-600 hover:text-stone-900',
                  )}
                  onClick={() => setFilter(tab.id)}
                >
                  <span className="block truncate">{tab.label}</span>
                  <span
                    className={cn(
                      'mt-0.5 block text-[11px] font-medium tabular-nums',
                      selected ? 'text-emerald-700' : 'text-stone-400',
                    )}
                  >
                    {counts[tab.id]}
                  </span>
                </button>
              )
            })}
          </div>

          {sessionsQuery.isError ? (
            <p className="mt-5 text-sm text-stone-600">
              Could not load your workout sessions. Try refreshing the page.
            </p>
          ) : filtered.length === 0 ? (
            <p className="mt-5 rounded-xl border border-dashed border-stone-200 bg-stone-50/80 px-4 py-6 text-center text-sm text-stone-600">
              {emptyCopy(activeFilter)}
            </p>
          ) : (
            <ul className="mt-4 divide-y divide-stone-100">
              {filtered.map((session) => {
                const isOpen =
                  session.status === 'PENDING' ||
                  session.status === 'IN_PROGRESS'
                return (
                  <li
                    key={session.sessionId}
                    className="flex flex-col gap-3 py-4 first:pt-2 last:pb-0 sm:flex-row sm:items-center sm:justify-between sm:gap-4"
                  >
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-base font-semibold text-stone-900">
                        {session.workoutTitle}
                      </p>
                      <p className="mt-0.5 text-sm text-stone-600">
                        Focus: {session.focusNodeName}
                        {session.planDayNumber != null &&
                        session.planDurationDays != null
                          ? ` · Day ${session.planDayNumber} of ${session.planDurationDays}`
                          : null}
                      </p>
                      {session.awaitingVerify ? (
                        <p className="mt-1 text-xs font-medium text-amber-800">
                          Plan done — verify skill to unlock next node
                        </p>
                      ) : null}
                      <p className="mt-1 text-xs text-stone-500">
                        {formatSessionDate(session.updatedAt)}
                      </p>
                    </div>
                    <Button
                      variant={isOpen ? 'primary' : 'secondary'}
                      className="w-full shrink-0 sm:w-auto"
                      onClick={() => {
                        if (isOpen) enterWorkout()
                        startTransition(() =>
                          navigate(`/sessions/${session.sessionId}`),
                        )
                      }}
                    >
                      {ctaLabel(session)}
                    </Button>
                  </li>
                )
              })}
            </ul>
          )}
        </section>
      </div>
    </PageShell>
  )
}
