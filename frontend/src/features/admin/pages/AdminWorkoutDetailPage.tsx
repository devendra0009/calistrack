import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { toast } from '@/shared/ui/notify'
import {
  useAddWorkoutExercise,
  useAdminExercises,
  useAdminNodes,
  useAdminWorkout,
  useDeleteWorkoutExercise,
  useUpdateWorkout,
} from '@/features/admin/api'
import { CATALOG_STATUS, DIFFICULTY } from '@/features/admin/constants'
import type { AdminWorkoutExerciseRequest } from '@/shared/api/types'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

export function AdminWorkoutDetailPage() {
  const { workoutId } = useParams<{ workoutId: string }>()
  const workout = useAdminWorkout(workoutId)
  const exercises = useAdminExercises('ACTIVE')
  const nodes = useAdminNodes('ACTIVE')
  const updateWorkout = useUpdateWorkout()
  const addLine = useAddWorkoutExercise()
  const removeLine = useDeleteWorkoutExercise()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [goalNodeId, setGoalNodeId] = useState('')
  const [difficulty, setDifficulty] = useState('BEGINNER')
  const [status, setStatus] = useState('ACTIVE')

  const [line, setLine] = useState<AdminWorkoutExerciseRequest>({
    exerciseId: '',
    sequence: 1,
    targetSets: 3,
    targetReps: 8,
    targetHoldSeconds: null,
    targetRestSeconds: 60,
    notes: '',
  })

  useEffect(() => {
    if (!workout.data) return
    setTitle(workout.data.title)
    setDescription(workout.data.description ?? '')
    setGoalNodeId(workout.data.goalNode.id)
    setDifficulty(workout.data.difficulty)
    setStatus(workout.data.status)
    const nextSeq =
      Math.max(0, ...workout.data.exercises.map((e) => e.sequence)) + 1
    setLine((prev) => ({
      ...prev,
      sequence: nextSeq,
      exerciseId: prev.exerciseId || exercises.data?.[0]?.id || '',
    }))
  }, [workout.data, exercises.data])

  if (workout.isLoading) {
    return <Spinner label="Loading workout…" />
  }

  if (workout.isError || !workout.data) {
    return (
      <div className="space-y-3">
        <p className="text-sm text-red-600">Workout not found.</p>
        <Link to="/admin/workouts" className="text-sm font-semibold text-emerald-900">
          ← Back to workouts
        </Link>
      </div>
    )
  }

  const data = workout.data

  return (
    <div className="space-y-6">
      <div>
        <Link
          to="/admin/workouts"
          className="text-sm font-medium text-emerald-900 hover:underline"
        >
          ← Workouts
        </Link>
        <h1 className="mt-2 text-2xl font-bold text-stone-900">{data.title}</h1>
        <p className="mt-1 text-sm text-stone-600">
          Goal: <span className="font-medium">{data.goalNode.name}</span>
        </p>
      </div>

      <form
        className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm"
        onSubmit={async (e) => {
          e.preventDefault()
          try {
            await updateWorkout.mutateAsync({
              id: data.id,
              body: {
                title,
                description,
                goalNodeId,
                difficulty,
                status,
                exercises: null,
              },
            })
            toast.success('Workout saved')
          } catch (err) {
            toast.error(err instanceof ApiError ? err.message : 'Save failed')
          }
        }}
      >
        <h2 className="font-semibold text-stone-900">Workout details</h2>
        <Input label="Title" value={title} onChange={(e) => setTitle(e.target.value)} required />
        <label className="flex flex-col gap-1.5 text-sm">
          <span className="font-medium text-stone-800">Description</span>
          <textarea
            className="min-h-20 rounded-lg border border-stone-300 bg-stone-50 px-3 py-2 outline-none ring-emerald-600/30 focus:ring-2"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </label>
        <SelectField
          label="Attached goal / node"
          value={goalNodeId}
          onChange={(e) => setGoalNodeId(e.target.value)}
          options={(nodes.data ?? []).map((n) => ({ value: n.id, label: n.name }))}
        />
        <div className="grid gap-3 sm:grid-cols-2">
          <SelectField
            label="Difficulty"
            value={difficulty}
            onChange={(e) => setDifficulty(e.target.value)}
            options={[...DIFFICULTY]}
          />
          <SelectField
            label="Status"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            options={[...CATALOG_STATUS]}
          />
        </div>
        <Button type="submit" loading={updateWorkout.isPending}>
          Save details
        </Button>
      </form>

      <section className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm">
        <h2 className="font-semibold text-stone-900">Exercise lines</h2>
        <ul className="space-y-2">
          {data.exercises.map((ex) => (
            <li
              key={ex.id}
              className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-stone-100 bg-stone-50 px-3 py-2"
            >
              <div>
                <p className="text-sm font-semibold text-stone-900">
                  #{ex.sequence} {ex.exercise.name}
                </p>
                <p className="text-xs text-stone-500">
                  sets {ex.targetSets ?? '—'} · reps {ex.targetReps ?? '—'} · hold{' '}
                  {ex.targetHoldSeconds ?? '—'}s · rest {ex.targetRestSeconds ?? '—'}s
                </p>
              </div>
              <Button
                type="button"
                variant="ghost"
                onClick={async () => {
                  try {
                    await removeLine.mutateAsync({
                      workoutId: data.id,
                      workoutExerciseId: ex.id,
                    })
                    toast.success('Line removed')
                  } catch (err) {
                    toast.error(
                      err instanceof ApiError ? err.message : 'Remove failed',
                    )
                  }
                }}
              >
                Remove
              </Button>
            </li>
          ))}
          {!data.exercises.length ? (
            <p className="text-sm text-stone-500">No exercise lines yet.</p>
          ) : null}
        </ul>

        <form
          className="space-y-3 border-t border-stone-200 pt-4"
          onSubmit={async (e) => {
            e.preventDefault()
            if (!line.exerciseId) {
              toast.error('Pick an exercise')
              return
            }
            try {
              await addLine.mutateAsync({ workoutId: data.id, body: line })
              toast.success('Exercise added')
            } catch (err) {
              toast.error(err instanceof ApiError ? err.message : 'Add failed')
            }
          }}
        >
          <h3 className="text-sm font-semibold text-stone-800">Add exercise line</h3>
          <SelectField
            label="Exercise"
            value={line.exerciseId}
            onChange={(e) => setLine({ ...line, exerciseId: e.target.value })}
            options={[
              { value: '', label: 'Select…' },
              ...(exercises.data ?? []).map((ex) => ({
                value: ex.id,
                label: ex.name,
              })),
            ]}
          />
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            <Input
              label="Sequence"
              type="number"
              min={1}
              value={line.sequence}
              onChange={(e) =>
                setLine({ ...line, sequence: Number(e.target.value) })
              }
              required
            />
            <Input
              label="Target sets"
              type="number"
              value={line.targetSets ?? ''}
              onChange={(e) =>
                setLine({
                  ...line,
                  targetSets: e.target.value === '' ? null : Number(e.target.value),
                })
              }
            />
            <Input
              label="Target reps"
              type="number"
              value={line.targetReps ?? ''}
              onChange={(e) =>
                setLine({
                  ...line,
                  targetReps: e.target.value === '' ? null : Number(e.target.value),
                })
              }
            />
            <Input
              label="Hold seconds"
              type="number"
              value={line.targetHoldSeconds ?? ''}
              onChange={(e) =>
                setLine({
                  ...line,
                  targetHoldSeconds:
                    e.target.value === '' ? null : Number(e.target.value),
                })
              }
            />
            <Input
              label="Rest seconds"
              type="number"
              value={line.targetRestSeconds ?? ''}
              onChange={(e) =>
                setLine({
                  ...line,
                  targetRestSeconds:
                    e.target.value === '' ? null : Number(e.target.value),
                })
              }
            />
          </div>
          <Input
            label="Notes"
            value={line.notes ?? ''}
            onChange={(e) => setLine({ ...line, notes: e.target.value })}
          />
          <Button type="submit" loading={addLine.isPending}>
            Add to workout
          </Button>
        </form>
      </section>
    </div>
  )
}
