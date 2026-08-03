import { useQuery } from '@tanstack/react-query'
import { startTransition, useState } from 'react'
import { useNavigate } from 'react-router'
import { toast } from 'sonner'
import { ApiError } from '@/shared/api/errors'
import type { CatalogGoal } from '@/shared/api/types'
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
  const selectedGoal = items.find((g) => g.id === selected) ?? null

  return (
    <PageShell
      title="Choose your goal"
      subtitle="Swipe to browse skills, tap one, then continue."
    >
      {items.length === 0 ? (
        <p className="text-sm text-stone-600">No active goal nodes in the catalog yet.</p>
      ) : (
        <div className="space-y-5">
          <div
            className={cn(
              'grid grid-flow-col grid-rows-3 gap-3 overflow-x-auto overscroll-x-contain pb-2',
              'auto-cols-[minmax(9.5rem,11.5rem)] snap-x snap-mandatory scroll-smooth scrollbar-thin',
              'sm:auto-cols-[minmax(11rem,14rem)]',
            )}
            role="listbox"
            aria-label="Goal skills"
            aria-activedescendant={selected ? `goal-${selected}` : undefined}
          >
            {items.map((goal) => (
              <GoalCard
                key={goal.id}
                goal={goal}
                selected={selected === goal.id}
                onSelect={() => setSelected(goal.id)}
              />
            ))}
          </div>

          {selectedGoal ? (
            <div className="rounded-2xl border border-emerald-200 bg-emerald-50/70 px-4 py-3">
              <p className="text-xs font-semibold uppercase tracking-wide text-emerald-800">
                Selected
              </p>
              <p className="mt-1 text-base font-semibold text-stone-900">
                {selectedGoal.name}
              </p>
              {selectedGoal.description ? (
                <p className="mt-1 text-sm text-stone-600">
                  {selectedGoal.description}
                </p>
              ) : null}
            </div>
          ) : (
            <p className="text-center text-sm text-stone-500">
              Select a skill card to continue.
            </p>
          )}
        </div>
      )}

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

function GoalCard({
  goal,
  selected,
  onSelect,
}: {
  goal: CatalogGoal
  selected: boolean
  onSelect: () => void
}) {
  return (
    <button
      id={`goal-${goal.id}`}
      type="button"
      role="option"
      aria-selected={selected}
      onClick={onSelect}
      className={cn(
        'flex h-full min-h-24 w-full snap-start flex-col rounded-2xl border px-3 py-2.5 text-left transition',
        'touch-manipulation sm:min-h-28 sm:px-4 sm:py-3',
        selected
          ? 'border-emerald-700 bg-emerald-50 ring-2 ring-emerald-700/25'
          : 'border-stone-200 bg-stone-50 hover:border-stone-300',
      )}
    >
      {goal.difficulty ? (
        <span
          className={cn(
            'w-fit rounded-md px-2 py-0.5 text-[11px] font-medium',
            selected
              ? 'bg-emerald-100 text-emerald-800'
              : 'bg-stone-100 text-stone-600',
          )}
        >
          {goal.difficulty}
        </span>
      ) : (
        <span className="text-[11px] font-medium uppercase tracking-wide text-stone-400">
          Skill
        </span>
      )}
      <h2 className="mt-2 text-sm font-semibold leading-snug text-stone-900 sm:text-base">
        {goal.name}
      </h2>
      {goal.description ? (
        <p className="mt-1.5 line-clamp-3 text-xs text-stone-600 sm:text-sm">
          {goal.description}
        </p>
      ) : (
        <p className="mt-1.5 text-xs text-stone-400 sm:text-sm">Tap to select</p>
      )}
      {selected ? (
        <span className="mt-auto pt-2 text-xs font-semibold text-emerald-800">
          Selected
        </span>
      ) : null}
    </button>
  )
}
