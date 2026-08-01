import { useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { toast } from 'sonner'
import {
  useAdminNodes,
  useAdminWorkoutPlans,
  useCreateWorkout,
  useCreateWorkoutPlan,
  useDeprecateWorkoutPlan,
} from '@/features/admin/api'
import { DIFFICULTY } from '@/features/admin/constants'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

export function AdminWorkoutPlansPage() {
  const navigate = useNavigate()
  const plans = useAdminWorkoutPlans()
  const nodes = useAdminNodes('ACTIVE')
  const createWorkout = useCreateWorkout()
  const createPlan = useCreateWorkoutPlan()
  const deprecate = useDeprecateWorkoutPlan()

  const [showForm, setShowForm] = useState(false)
  const [nodeId, setNodeId] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [durationDays, setDurationDays] = useState(7)
  const [difficulty, setDifficulty] = useState('BEGINNER')
  const [creating, setCreating] = useState(false)

  const nodeName = useMemo(
    () => nodes.data?.find((n) => n.id === nodeId)?.name ?? 'Skill',
    [nodes.data, nodeId],
  )

  function startCreate() {
    setNodeId(nodes.data?.[0]?.id ?? '')
    setTitle('')
    setDescription('')
    setDurationDays(7)
    setDifficulty('BEGINNER')
    setShowForm(true)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!nodeId) {
      toast.error('Pick a skill node')
      return
    }
    if (durationDays < 1 || durationDays > 90) {
      toast.error('Duration must be between 1 and 90 days')
      return
    }

    const planTitle =
      title.trim() || `${nodeName} — ${durationDays} Day Plan`

    setCreating(true)
    try {
      // One dedicated workout shell per day (empty exercises — fill on detail page).
      const dayWorkouts = []
      for (let day = 1; day <= durationDays; day += 1) {
        const workout = await createWorkout.mutateAsync({
          title: `${nodeName} — Day ${day}`,
          description: `Day ${day} of ${planTitle}`,
          goalNodeId: nodeId,
          difficulty,
          status: 'ACTIVE',
          exercises: [],
        })
        dayWorkouts.push(workout)
      }

      const created = await createPlan.mutateAsync({
        title: planTitle,
        description: description.trim() || null,
        nodeId,
        durationDays,
        status: 'ACTIVE',
        days: dayWorkouts.map((w, i) => ({
          dayNumber: i + 1,
          workoutId: w.id,
        })),
      })

      toast.success('Plan shell created — build each day’s exercises next')
      setShowForm(false)
      navigate(`/admin/workout-plans/${created.id}`)
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Create failed')
    } finally {
      setCreating(false)
    }
  }

  if (plans.isLoading || nodes.isLoading) {
    return <Spinner />
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-emerald-900">
            Step 3b
          </p>
          <h1 className="text-2xl font-bold text-stone-900">Workout plans</h1>
          <p className="mt-1 text-sm text-stone-600">
            Pick a skill node and day count. Each day gets its own workout so you
            can progress exercises, sets, and reps across the plan.
          </p>
        </div>
        <Button
          type="button"
          onClick={startCreate}
          disabled={!nodes.data?.length}
        >
          New plan
        </Button>
      </header>

      {!nodes.data?.length ? (
        <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Create a goal node in Step 2 before adding plans.
        </p>
      ) : null}

      {showForm ? (
        <form
          onSubmit={onSubmit}
          className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm"
        >
          <h2 className="font-semibold text-stone-900">Create plan shell</h2>
          <SelectField
            label="Skill node"
            value={nodeId}
            onChange={(e) => setNodeId(e.target.value)}
            options={[
              { value: '', label: 'Select node…' },
              ...(nodes.data ?? []).map((n) => ({ value: n.id, label: n.name })),
            ]}
            required
          />
          <Input
            label="Plan title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder={`${nodeName} — ${durationDays} Day Plan`}
          />
          <Input
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <div className="grid gap-3 sm:grid-cols-2">
            <Input
              label="How many days?"
              type="number"
              min={1}
              max={90}
              value={durationDays}
              onChange={(e) => setDurationDays(Number(e.target.value) || 1)}
              required
            />
            <SelectField
              label="Default difficulty for day workouts"
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              options={[...DIFFICULTY]}
            />
          </div>
          <p className="text-xs text-stone-500">
            Creates {durationDays} empty day workouts for this node, then opens
            the builder so you can add exercises (and copy Day N → Day N+1).
          </p>
          <div className="flex gap-2">
            <Button type="submit" loading={creating}>
              Create &amp; build days
            </Button>
            <Button
              type="button"
              variant="secondary"
              disabled={creating}
              onClick={() => setShowForm(false)}
            >
              Cancel
            </Button>
          </div>
        </form>
      ) : null}

      <ul className="divide-y divide-stone-100 rounded-2xl border border-stone-200 bg-stone-50/90">
        {(plans.data ?? []).map((plan) => (
          <li
            key={plan.id}
            className="flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="min-w-0">
              <Link
                to={`/admin/workout-plans/${plan.id}`}
                className="font-semibold text-stone-900 hover:underline"
              >
                {plan.title}
              </Link>
              <p className="mt-0.5 text-sm text-stone-600">
                {plan.node.name} · {plan.dayCount}/{plan.durationDays} days ·{' '}
                {plan.status}
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate(`/admin/workout-plans/${plan.id}`)}
              >
                Build days
              </Button>
              {plan.status !== 'DEPRECATED' ? (
                <Button
                  type="button"
                  variant="secondary"
                  loading={deprecate.isPending}
                  onClick={async () => {
                    try {
                      await deprecate.mutateAsync(plan.id)
                      toast.success('Plan deprecated')
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
        {!plans.data?.length ? (
          <li className="px-4 py-8 text-center text-sm text-stone-500">
            No workout plans yet.
          </li>
        ) : null}
      </ul>
    </div>
  )
}
