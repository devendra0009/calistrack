import { useQuery } from '@tanstack/react-query'
import { startTransition, useState } from 'react'
import { useNavigate } from 'react-router'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { listGoals } from '@/features/catalog/api'
import { usePutGoal } from '@/features/profile/api'
import { cn } from '@/shared/lib/cn'

export function GoalPage() {
  const navigate = useNavigate()
  const putGoal = usePutGoal()
  const [selected, setSelected] = useState<string | null>(null)

  const goals = useQuery({
    queryKey: ['catalog', 'goals'],
    queryFn: listGoals,
  })

  if (goals.isLoading) {
    return (
      <PageShell title="Choose your goal" subtitle="Pick the skill you want to unlock.">
        <Spinner />
      </PageShell>
    )
  }

  if (goals.isError) {
    return (
      <PageShell title="Choose your goal" subtitle="Pick the skill you want to unlock.">
        <p className="text-sm text-stone-600">
          Could not load goals. Check that the API is running and try again.
        </p>
      </PageShell>
    )
  }

  const items = goals.data ?? []

  return (
    <PageShell
      title="Choose your goal"
      subtitle="We'll ask a few placement questions, then create your first pending workout session."
    >
      {items.length === 0 ? (
        <p className="text-sm text-stone-600">No active goal nodes in the catalog yet.</p>
      ) : null}

      <div className="space-y-3">
        {items.map((goal) => {
          const active = selected === goal.id
          return (
            <button
              key={goal.id}
              type="button"
              onClick={() => setSelected(goal.id)}
              className={cn(
                'w-full rounded-2xl border px-5 py-4 text-left transition',
                active
                  ? 'border-emerald-700 bg-emerald-50 ring-2 ring-emerald-700/20'
                  : 'border-stone-200 bg-stone-50 hover:border-stone-300',
              )}
            >
              <div className="flex items-center justify-between gap-3">
                <h2 className="text-lg font-semibold text-stone-900">{goal.name}</h2>
                {goal.difficulty ? (
                  <span className="rounded-md bg-stone-100 px-2 py-0.5 text-xs font-medium text-stone-600">
                    {goal.difficulty}
                  </span>
                ) : null}
              </div>
              {goal.description ? (
                <p className="mt-1 text-sm text-stone-600">{goal.description}</p>
              ) : null}
            </button>
          )
        })}
      </div>

      <div className="mt-6">
        <Button
          disabled={!selected}
          loading={putGoal.isPending}
          onClick={async () => {
            if (!selected) return
            try {
              await putGoal.mutateAsync(selected)
              toast.success('Goal saved')
              startTransition(() => navigate('/setup/questions'))
            } catch (err) {
              toast.error(
                err instanceof ApiError ? err.message : 'Could not set goal',
              )
            }
          }}
        >
          Continue
        </Button>
      </div>
    </PageShell>
  )
}
