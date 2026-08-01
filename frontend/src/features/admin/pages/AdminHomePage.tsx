import { Link } from 'react-router'
import { ADMIN_STEPS } from '@/features/admin/constants'
import {
  useAdminEdges,
  useAdminExercises,
  useAdminNodes,
  useAdminPathQuestions,
  useAdminWorkoutPlans,
  useAdminWorkouts,
} from '@/features/admin/api'
import { Spinner } from '@/shared/ui/Spinner'

export function AdminHomePage() {
  const exercises = useAdminExercises()
  const nodes = useAdminNodes()
  const workouts = useAdminWorkouts()
  const plans = useAdminWorkoutPlans()
  const edges = useAdminEdges()
  const pathQuestions = useAdminPathQuestions()

  const loading =
    exercises.isLoading ||
    nodes.isLoading ||
    workouts.isLoading ||
    plans.isLoading ||
    edges.isLoading ||
    pathQuestions.isLoading

  const activeExercises =
    exercises.data?.filter((e) => e.status === 'ACTIVE').length ?? 0
  const activeNodes = nodes.data?.filter((n) => n.status === 'ACTIVE').length ?? 0
  const activeWorkouts =
    workouts.data?.filter((w) => w.status === 'ACTIVE').length ?? 0
  const activePlans =
    plans.data?.filter((p) => p.status === 'ACTIVE').length ?? 0
  const edgeCount = edges.data?.length ?? 0
  const questionCount = pathQuestions.data?.length ?? 0

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-stone-900">
          Catalog builder
        </h1>
        <p className="mt-2 max-w-2xl text-stone-600">
          Build the path users train on: exercises → goals → workouts → multi-day
          plans → edges → placement questions. Day complete unlocks the next day;
          plan complete + skill verify unlocks the next node.
        </p>
      </div>

      {loading ? (
        <Spinner label="Loading catalog…" />
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <Stat label="Exercises" value={activeExercises} />
          <Stat label="Goals / nodes" value={activeNodes} />
          <Stat label="Workouts" value={activeWorkouts} />
          <Stat label="Plans" value={activePlans} />
          <Stat label="Path edges" value={edgeCount} />
          <Stat label="Questions" value={questionCount} />
        </div>
      )}

      <ol className="space-y-3">
        {ADMIN_STEPS.map((s) => (
          <li key={s.to}>
            <Link
              to={s.to}
              className="flex gap-4 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm transition hover:border-emerald-300"
            >
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-900">
                {s.step}
              </span>
              <div>
                <p className="font-semibold text-stone-900">{s.title}</p>
                <p className="mt-1 text-sm text-stone-600">{s.blurb}</p>
              </div>
            </Link>
          </li>
        ))}
      </ol>
    </div>
  )
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-2xl border border-stone-200 bg-stone-50/90 p-4 shadow-sm">
      <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
        {label}
      </p>
      <p className="mt-1 text-2xl font-bold text-stone-900">{value}</p>
    </div>
  )
}
