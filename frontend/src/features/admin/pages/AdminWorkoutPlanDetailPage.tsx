import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router'
import { useQueryClient } from '@tanstack/react-query'
import { toast } from '@/shared/ui/notify'
import {
  adminKeys,
  useAdminExercises,
  useAdminWorkout,
  useAdminWorkoutPlan,
  useCreateWorkout,
  useUpdateWorkout,
  useUpdateWorkoutPlan,
} from '@/features/admin/api'
import { DIFFICULTY } from '@/features/admin/constants'
import type {
  AdminWorkoutExerciseRequest,
  AdminWorkoutPlanDayRequest,
  AdminWorkoutResponse,
} from '@/shared/api/types'
import { api } from '@/shared/api/client'
import { ApiError } from '@/shared/api/errors'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

type DraftLine = AdminWorkoutExerciseRequest & { key: string }

function toDraftLines(workout: AdminWorkoutResponse): DraftLine[] {
  return workout.exercises.map((ex, i) => ({
    key: ex.id || `line-${i}`,
    exerciseId: ex.exercise.id,
    sequence: ex.sequence,
    targetSets: ex.targetSets,
    targetReps: ex.targetReps,
    targetHoldSeconds: ex.targetHoldSeconds,
    targetRestSeconds: ex.targetRestSeconds,
    notes: ex.notes,
    demoVideoUrl: ex.demoVideoUrl,
  }))
}

function emptyLine(sequence: number, exerciseId = ''): DraftLine {
  return {
    key: `new-${Date.now()}-${sequence}`,
    exerciseId,
    sequence,
    targetSets: 3,
    targetReps: 8,
    targetHoldSeconds: null,
    targetRestSeconds: 60,
    notes: '',
  }
}

export function AdminWorkoutPlanDetailPage() {
  const { planId } = useParams<{ planId: string }>()
  const qc = useQueryClient()
  const planQuery = useAdminWorkoutPlan(planId)
  const exercises = useAdminExercises('ACTIVE')
  const updatePlan = useUpdateWorkoutPlan()
  const createWorkout = useCreateWorkout()
  const updateWorkout = useUpdateWorkout()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [durationDays, setDurationDays] = useState(7)
  const [status, setStatus] = useState('ACTIVE')
  const [days, setDays] = useState<AdminWorkoutPlanDayRequest[]>([])
  const [selectedDay, setSelectedDay] = useState(1)
  const [copyFromDay, setCopyFromDay] = useState(1)
  const [savingMeta, setSavingMeta] = useState(false)
  const [savingDay, setSavingDay] = useState(false)
  const [extending, setExtending] = useState(false)

  const selectedWorkoutId = useMemo(
    () => days.find((d) => d.dayNumber === selectedDay)?.workoutId,
    [days, selectedDay],
  )

  const dayWorkout = useAdminWorkout(selectedWorkoutId)

  const [dayTitle, setDayTitle] = useState('')
  const [dayDifficulty, setDayDifficulty] = useState('BEGINNER')
  const [draftLines, setDraftLines] = useState<DraftLine[]>([])
  const [dirty, setDirty] = useState(false)

  useEffect(() => {
    if (!planQuery.data) return
    setTitle(planQuery.data.title)
    setDescription(planQuery.data.description ?? '')
    setDurationDays(planQuery.data.durationDays)
    setStatus(planQuery.data.status)
    const mapped = planQuery.data.days.map((d) => ({
      dayNumber: d.dayNumber,
      workoutId: d.workout.id,
    }))
    setDays(mapped)
    setSelectedDay((prev) =>
      mapped.some((d) => d.dayNumber === prev) ? prev : 1,
    )
    setCopyFromDay((prev) => (prev >= 2 ? prev : 1))
  }, [planQuery.data])

  useEffect(() => {
    if (!dayWorkout.data) return
    setDayTitle(dayWorkout.data.title)
    setDayDifficulty(dayWorkout.data.difficulty)
    setDraftLines(toDraftLines(dayWorkout.data))
    setDirty(false)
  }, [dayWorkout.data])

  const exerciseOptions = useMemo(
    () => [
      { value: '', label: 'Select exercise…' },
      ...(exercises.data ?? []).map((ex) => ({ value: ex.id, label: ex.name })),
    ],
    [exercises.data],
  )

  const exerciseName = (id: string) =>
    exercises.data?.find((e) => e.id === id)?.name ?? 'Exercise'

  async function savePlanMeta() {
    if (!planQuery.data) return
    if (days.some((d) => !d.workoutId) || days.length !== durationDays) {
      toast.error('Every day needs a workout before saving the plan')
      return
    }
    setSavingMeta(true)
    try {
      await updatePlan.mutateAsync({
        id: planQuery.data.id,
        body: {
          title: title.trim(),
          description: description.trim() || null,
          nodeId: planQuery.data.node.id,
          durationDays,
          status,
          days,
        },
      })
      toast.success('Plan details saved')
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Save failed')
    } finally {
      setSavingMeta(false)
    }
  }

  async function applyDuration(nextDuration: number) {
    if (!planQuery.data) return
    const next = Math.max(1, Math.min(90, nextDuration))
    if (next === days.length) {
      setDurationDays(next)
      return
    }

    setExtending(true)
    try {
      let nextDays = [...days]
      if (next > days.length) {
        for (let day = days.length + 1; day <= next; day += 1) {
          const workout = await createWorkout.mutateAsync({
            title: `${planQuery.data.node.name} — Day ${day}`,
            description: `Day ${day} of ${title || planQuery.data.title}`,
            goalNodeId: planQuery.data.node.id,
            difficulty: dayDifficulty || 'BEGINNER',
            status: 'ACTIVE',
            exercises: [],
          })
          nextDays.push({ dayNumber: day, workoutId: workout.id })
        }
      } else {
        nextDays = days.filter((d) => d.dayNumber <= next)
      }

      setDays(nextDays)
      setDurationDays(next)
      if (selectedDay > next) setSelectedDay(next)

      await updatePlan.mutateAsync({
        id: planQuery.data.id,
        body: {
          title: title.trim() || planQuery.data.title,
          description: description.trim() || null,
          nodeId: planQuery.data.node.id,
          durationDays: next,
          status,
          days: nextDays,
        },
      })
      toast.success(
        next > days.length
          ? `Added days ${days.length + 1}–${next}`
          : `Plan shortened to ${next} days`,
      )
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not change duration')
    } finally {
      setExtending(false)
    }
  }

  async function saveSelectedDay() {
    if (!planQuery.data || !selectedWorkoutId || !dayWorkout.data) return
    if (draftLines.some((l) => !l.exerciseId)) {
      toast.error('Every line needs an exercise')
      return
    }
    setSavingDay(true)
    try {
      const normalized = draftLines.map((line, i) => ({
        exerciseId: line.exerciseId,
        sequence: i + 1,
        targetSets: line.targetSets ?? null,
        targetReps: line.targetReps ?? null,
        targetHoldSeconds: line.targetHoldSeconds ?? null,
        targetRestSeconds: line.targetRestSeconds ?? null,
        notes: line.notes?.trim() ? line.notes.trim() : null,
        demoVideoUrl: line.demoVideoUrl ?? null,
      }))
      await updateWorkout.mutateAsync({
        id: selectedWorkoutId,
        body: {
          title: dayTitle.trim() || `${planQuery.data.node.name} — Day ${selectedDay}`,
          description: dayWorkout.data.description,
          goalNodeId: planQuery.data.node.id,
          difficulty: dayDifficulty,
          status: 'ACTIVE',
          exercises: normalized,
        },
      })
      setDirty(false)
      toast.success(`Day ${selectedDay} saved`)
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Could not save day')
    } finally {
      setSavingDay(false)
    }
  }

  async function copyExercisesFromDay(sourceDay: number) {
    if (!planQuery.data) return
    if (sourceDay === selectedDay) {
      toast.error('Pick a different day to copy from')
      return
    }
    const source = days.find((d) => d.dayNumber === sourceDay)
    if (!source?.workoutId) {
      toast.error(`Day ${sourceDay} has no workout`)
      return
    }
    try {
      const workout = await qc.fetchQuery({
        queryKey: adminKeys.workout(source.workoutId),
        queryFn: () =>
          api.get<AdminWorkoutResponse>(
            `/api/v1/admin/workouts/${source.workoutId}`,
          ),
      })
      if (!workout.exercises.length) {
        toast.error(`Day ${sourceDay} has no exercises yet`)
        return
      }
      setDraftLines(
        workout.exercises.map((ex, i) => ({
          key: `copy-${sourceDay}-${i}-${Date.now()}`,
          exerciseId: ex.exercise.id,
          sequence: i + 1,
          targetSets: ex.targetSets,
          targetReps: ex.targetReps,
          targetHoldSeconds: ex.targetHoldSeconds,
          targetRestSeconds: ex.targetRestSeconds,
          notes: ex.notes,
          demoVideoUrl: ex.demoVideoUrl,
        })),
      )
      setDirty(true)
      toast.success(
        `Copied Day ${sourceDay} — tweak sets/reps, then Save day`,
      )
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Copy failed')
    }
  }

  if (planQuery.isLoading) {
    return <Spinner />
  }

  if (planQuery.isError || !planQuery.data) {
    return (
      <div className="space-y-3">
        <p className="text-red-600">
          {planQuery.error instanceof ApiError
            ? planQuery.error.message
            : 'Plan not found'}
        </p>
        <Link
          to="/admin/workout-plans"
          className="text-sm font-semibold text-emerald-900"
        >
          Back to plans
        </Link>
      </div>
    )
  }

  const plan = planQuery.data

  return (
    <div className="space-y-6">
      <div>
        <Link
          to="/admin/workout-plans"
          className="text-sm font-semibold text-emerald-900"
        >
          ← Workout plans
        </Link>
        <h1 className="mt-2 text-2xl font-bold text-stone-900">{plan.title}</h1>
        <p className="mt-1 text-sm text-stone-600">
          Node: {plan.node.name} — build a distinct workout for each day
        </p>
      </div>

      <section className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm">
        <h2 className="font-semibold text-stone-900">Plan details</h2>
        <Input
          label="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <Input
          label="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <div className="grid gap-3 sm:grid-cols-2">
          <Input
            label="Duration (days)"
            type="number"
            min={1}
            max={90}
            value={durationDays}
            onChange={(e) => setDurationDays(Number(e.target.value) || 1)}
            onBlur={() => {
              if (durationDays !== days.length) {
                void applyDuration(durationDays)
              }
            }}
          />
          <SelectField
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            options={[
              { value: 'ACTIVE', label: 'ACTIVE' },
              { value: 'DEPRECATED', label: 'DEPRECATED' },
            ]}
          />
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            type="button"
            loading={savingMeta}
            onClick={() => void savePlanMeta()}
          >
            Save plan details
          </Button>
          <Button
            type="button"
            variant="secondary"
            loading={extending}
            onClick={() => void applyDuration(durationDays)}
          >
            Apply day count
          </Button>
        </div>
      </section>

      <section className="space-y-4 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="font-semibold text-stone-900">Day builder</h2>
            <p className="mt-1 text-sm text-stone-600">
              Select a day, add exercises, or copy a previous day and adjust
              volume.
            </p>
          </div>
          {dirty ? (
            <p className="text-xs font-medium text-amber-800">Unsaved day edits</p>
          ) : null}
        </div>

        <div className="flex flex-wrap gap-1.5">
          {days.map((d) => (
            <button
              key={d.dayNumber}
              type="button"
              className={cn(
                'rounded-lg px-3 py-1.5 text-sm font-semibold transition',
                selectedDay === d.dayNumber
                  ? 'bg-emerald-800 text-white'
                  : 'bg-stone-100 text-stone-700 hover:bg-stone-200',
              )}
              onClick={() => {
                if (dirty) {
                  const ok = window.confirm(
                    'Discard unsaved changes for this day?',
                  )
                  if (!ok) return
                }
                setSelectedDay(d.dayNumber)
                setCopyFromDay(d.dayNumber > 1 ? d.dayNumber - 1 : 1)
              }}
            >
              Day {d.dayNumber}
            </button>
          ))}
        </div>

        {dayWorkout.isLoading ? (
          <Spinner label={`Loading day ${selectedDay}…`} />
        ) : dayWorkout.isError || !dayWorkout.data ? (
          <p className="text-sm text-red-600">Could not load this day’s workout.</p>
        ) : (
          <div className="space-y-4 border-t border-stone-100 pt-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <Input
                label={`Day ${selectedDay} workout title`}
                value={dayTitle}
                onChange={(e) => {
                  setDayTitle(e.target.value)
                  setDirty(true)
                }}
              />
              <SelectField
                label="Difficulty"
                value={dayDifficulty}
                onChange={(e) => {
                  setDayDifficulty(e.target.value)
                  setDirty(true)
                }}
                options={[...DIFFICULTY]}
              />
            </div>

            {selectedDay > 1 ? (
              <div className="flex flex-col gap-2 rounded-xl border border-dashed border-stone-200 bg-stone-50/80 p-3 sm:flex-row sm:items-end">
                <SelectField
                  label="Copy exercises from"
                  value={String(copyFromDay)}
                  onChange={(e) => setCopyFromDay(Number(e.target.value))}
                  options={days
                    .filter((d) => d.dayNumber !== selectedDay)
                    .map((d) => ({
                      value: String(d.dayNumber),
                      label: `Day ${d.dayNumber}`,
                    }))}
                />
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => void copyExercisesFromDay(copyFromDay)}
                >
                  Copy into Day {selectedDay}
                </Button>
              </div>
            ) : (
              <p className="text-xs text-stone-500">
                Build Day 1 from scratch. Later days can copy Day 1 (or any prior
                day) and then change sets/reps.
              </p>
            )}

            <ul className="space-y-3">
              {draftLines.map((line, index) => (
                <li
                  key={line.key}
                  className="space-y-2 rounded-xl border border-stone-200 bg-stone-50/60 p-3"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <p className="text-sm font-semibold text-stone-800">
                      #{index + 1}{' '}
                      {line.exerciseId
                        ? exerciseName(line.exerciseId)
                        : 'New exercise'}
                    </p>
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={() => {
                        setDraftLines((prev) =>
                          prev.filter((_, i) => i !== index),
                        )
                        setDirty(true)
                      }}
                    >
                      Remove
                    </Button>
                  </div>
                  <SelectField
                    label="Exercise"
                    value={line.exerciseId}
                    onChange={(e) => {
                      const exerciseId = e.target.value
                      setDraftLines((prev) =>
                        prev.map((l, i) =>
                          i === index ? { ...l, exerciseId } : l,
                        ),
                      )
                      setDirty(true)
                    }}
                    options={exerciseOptions}
                  />
                  <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-4">
                    <Input
                      label="Sets"
                      type="number"
                      value={line.targetSets ?? ''}
                      onChange={(e) => {
                        const targetSets =
                          e.target.value === '' ? null : Number(e.target.value)
                        setDraftLines((prev) =>
                          prev.map((l, i) =>
                            i === index ? { ...l, targetSets } : l,
                          ),
                        )
                        setDirty(true)
                      }}
                    />
                    <Input
                      label="Reps"
                      type="number"
                      value={line.targetReps ?? ''}
                      onChange={(e) => {
                        const targetReps =
                          e.target.value === '' ? null : Number(e.target.value)
                        setDraftLines((prev) =>
                          prev.map((l, i) =>
                            i === index ? { ...l, targetReps } : l,
                          ),
                        )
                        setDirty(true)
                      }}
                    />
                    <Input
                      label="Hold (s)"
                      type="number"
                      value={line.targetHoldSeconds ?? ''}
                      onChange={(e) => {
                        const targetHoldSeconds =
                          e.target.value === '' ? null : Number(e.target.value)
                        setDraftLines((prev) =>
                          prev.map((l, i) =>
                            i === index ? { ...l, targetHoldSeconds } : l,
                          ),
                        )
                        setDirty(true)
                      }}
                    />
                    <Input
                      label="Rest (s)"
                      type="number"
                      value={line.targetRestSeconds ?? ''}
                      onChange={(e) => {
                        const targetRestSeconds =
                          e.target.value === '' ? null : Number(e.target.value)
                        setDraftLines((prev) =>
                          prev.map((l, i) =>
                            i === index ? { ...l, targetRestSeconds } : l,
                          ),
                        )
                        setDirty(true)
                      }}
                    />
                  </div>
                  <Input
                    label="Notes"
                    value={line.notes ?? ''}
                    onChange={(e) => {
                      const notes = e.target.value
                      setDraftLines((prev) =>
                        prev.map((l, i) =>
                          i === index ? { ...l, notes } : l,
                        ),
                      )
                      setDirty(true)
                    }}
                  />
                </li>
              ))}
              {!draftLines.length ? (
                <p className="rounded-xl border border-dashed border-stone-200 px-4 py-6 text-center text-sm text-stone-500">
                  No exercises on Day {selectedDay} yet.
                </p>
              ) : null}
            </ul>

            <div className="flex flex-wrap gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setDraftLines((prev) => [
                    ...prev,
                    emptyLine(prev.length + 1, exercises.data?.[0]?.id ?? ''),
                  ])
                  setDirty(true)
                }}
              >
                Add exercise
              </Button>
              <Button
                type="button"
                loading={savingDay}
                onClick={() => void saveSelectedDay()}
              >
                Save day {selectedDay}
              </Button>
            </div>
          </div>
        )}
      </section>
    </div>
  )
}
