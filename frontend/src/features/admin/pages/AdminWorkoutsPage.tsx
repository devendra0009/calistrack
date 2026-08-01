import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { toast } from 'sonner'
import {
  useAdminNodes,
  useAdminWorkouts,
  useCreateWorkout,
  useDeprecateWorkout,
} from '@/features/admin/api'
import { CATALOG_STATUS, DIFFICULTY } from '@/features/admin/constants'
import type { AdminWorkoutRequest } from '@/shared/api/types'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

export function AdminWorkoutsPage() {
  const navigate = useNavigate()
  const workouts = useAdminWorkouts()
  const nodes = useAdminNodes('ACTIVE')
  const create = useCreateWorkout()
  const deprecate = useDeprecateWorkout()

  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<AdminWorkoutRequest>({
    title: '',
    description: '',
    goalNodeId: '',
    difficulty: 'BEGINNER',
    status: 'ACTIVE',
    exercises: [],
  })

  function startCreate() {
    setForm({
      title: '',
      description: '',
      goalNodeId: nodes.data?.[0]?.id ?? '',
      difficulty: 'BEGINNER',
      status: 'ACTIVE',
      exercises: [],
    })
    setShowForm(true)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!form.goalNodeId) {
      toast.error('Pick a goal node for this workout')
      return
    }
    try {
      const created = await create.mutateAsync(form)
      toast.success('Workout created — add exercise lines next')
      setShowForm(false)
      navigate(`/admin/workouts/${created.id}`)
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Create failed')
    }
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-emerald-900">
            Step 3
          </p>
          <h1 className="text-2xl font-bold text-stone-900">Workouts</h1>
          <p className="mt-1 text-sm text-stone-600">
            Create a workout attached to a goal, then connect exercise lines on
            the detail page.
          </p>
        </div>
        <Button
          type="button"
          onClick={startCreate}
          disabled={!nodes.data?.length}
        >
          New workout
        </Button>
      </header>

      {!nodes.data?.length && !nodes.isLoading ? (
        <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Create a goal in Step 2 before adding workouts.
        </p>
      ) : null}

      {showForm ? (
        <form
          onSubmit={onSubmit}
          className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm"
        >
          <h2 className="font-semibold text-stone-900">Create workout</h2>
          <Input
            label="Title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
            placeholder="e.g. Planche foundations"
          />
          <label className="flex flex-col gap-1.5 text-sm">
            <span className="font-medium text-stone-800">Description</span>
            <textarea
              className="min-h-20 rounded-lg border border-stone-300 bg-stone-50 px-3 py-2 outline-none ring-emerald-600/30 focus:ring-2"
              value={form.description ?? ''}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </label>
          <SelectField
            label="Goal / node"
            value={form.goalNodeId}
            onChange={(e) => setForm({ ...form, goalNodeId: e.target.value })}
            options={[
              { value: '', label: 'Select…' },
              ...(nodes.data ?? []).map((n) => ({ value: n.id, label: n.name })),
            ]}
            required
          />
          <div className="grid gap-3 sm:grid-cols-2">
            <SelectField
              label="Difficulty"
              value={form.difficulty}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
              options={[...DIFFICULTY]}
            />
            <SelectField
              label="Status"
              value={form.status ?? 'ACTIVE'}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
              options={[...CATALOG_STATUS]}
            />
          </div>
          <div className="flex gap-2 pt-2">
            <Button type="submit" loading={create.isPending}>
              Create & add exercises
            </Button>
            <Button type="button" variant="ghost" onClick={() => setShowForm(false)}>
              Cancel
            </Button>
          </div>
        </form>
      ) : null}

      {workouts.isLoading ? (
        <Spinner />
      ) : (
        <ul className="space-y-2">
          {(workouts.data ?? []).map((w) => (
            <li
              key={w.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-stone-200 bg-stone-50/90 px-4 py-3 shadow-sm"
            >
              <div>
                <p className="font-semibold text-stone-900">{w.title}</p>
                <p className="text-xs text-stone-500">
                  Goal: {w.goalNode.name} · {w.exerciseCount} exercise
                  {w.exerciseCount === 1 ? '' : 's'} · {w.difficulty} · {w.status}
                </p>
              </div>
              <div className="flex gap-2">
                <Link
                  to={`/admin/workouts/${w.id}`}
                  className="inline-flex items-center justify-center rounded-lg border border-stone-300 bg-stone-50 px-4 py-2.5 text-sm font-semibold text-stone-900 hover:bg-stone-50"
                >
                  Manage
                </Link>
                {w.status !== 'DEPRECATED' ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={async () => {
                      try {
                        await deprecate.mutateAsync(w.id)
                        toast.success('Workout deprecated')
                      } catch (err) {
                        toast.error(
                          err instanceof ApiError ? err.message : 'Deprecate failed',
                        )
                      }
                    }}
                  >
                    Deprecate
                  </Button>
                ) : null}
              </div>
            </li>
          ))}
          {!workouts.data?.length ? (
            <p className="text-sm text-stone-500">No workouts yet.</p>
          ) : null}
        </ul>
      )}
    </div>
  )
}
