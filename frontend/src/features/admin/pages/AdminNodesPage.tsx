import { useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import {
  useAdminExercises,
  useAdminNodes,
  useCreateNode,
  useDeprecateNode,
  useUpdateNode,
} from '@/features/admin/api'
import {
  CATALOG_STATUS,
  DIFFICULTY,
  NODE_TYPE,
  OPERATOR,
} from '@/features/admin/constants'
import type { AdminNodeRequest, AdminNodeResponse } from '@/shared/api/types'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

function emptyForm(exerciseId = ''): AdminNodeRequest {
  return {
    name: '',
    description: '',
    nodeType: 'MILESTONE',
    exerciseId,
    targetValue: 1,
    operator: '>=',
    unitLabel: 'reps',
    difficulty: 'BEGINNER',
    xpReward: 100,
    estimatedMinutes: 30,
    status: 'ACTIVE',
  }
}

export function AdminNodesPage() {
  const nodes = useAdminNodes()
  const exercises = useAdminExercises('ACTIVE')
  const create = useCreateNode()
  const update = useUpdateNode()
  const deprecate = useDeprecateNode()

  const [form, setForm] = useState<AdminNodeRequest>(emptyForm())
  const [editingId, setEditingId] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)

  const exerciseOptions = (exercises.data ?? []).map((e) => ({
    value: e.id,
    label: e.name,
  }))

  function startCreate() {
    const first = exercises.data?.[0]?.id ?? ''
    setEditingId(null)
    setForm(emptyForm(first))
    setShowForm(true)
  }

  function startEdit(node: AdminNodeResponse) {
    setEditingId(node.id)
    setForm({
      name: node.name,
      description: node.description ?? '',
      nodeType: node.nodeType,
      exerciseId: node.exercise.id,
      targetValue: node.targetValue,
      operator: node.operator,
      unitLabel: node.unitLabel,
      difficulty: node.difficulty,
      xpReward: node.xpReward,
      estimatedMinutes: node.estimatedMinutes,
      status: node.status,
    })
    setShowForm(true)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!form.exerciseId) {
      toast.error('Create an exercise first, then link it here')
      return
    }
    try {
      if (editingId) {
        await update.mutateAsync({ id: editingId, body: form })
        toast.success('Goal updated')
      } else {
        await create.mutateAsync(form)
        toast.success('Goal created')
      }
      setShowForm(false)
      setEditingId(null)
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : 'Save failed')
    }
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-emerald-900">
            Step 2
          </p>
          <h1 className="text-2xl font-bold text-stone-900">Goals / Nodes</h1>
          <p className="mt-1 text-sm text-stone-600">
            Each goal references an exercise (the skill being measured).
          </p>
        </div>
        <Button
          type="button"
          onClick={startCreate}
          disabled={!exercises.data?.length}
        >
          New goal
        </Button>
      </header>

      {!exercises.data?.length && !exercises.isLoading ? (
        <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Create at least one exercise in Step 1 before adding goals.
        </p>
      ) : null}

      {showForm ? (
        <form
          onSubmit={onSubmit}
          className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm"
        >
          <h2 className="font-semibold text-stone-900">
            {editingId ? 'Edit goal' : 'Create goal'}
          </h2>
          <Input
            label="Name"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
            placeholder="e.g. Planche"
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
            label="Linked exercise"
            value={form.exerciseId}
            onChange={(e) => setForm({ ...form, exerciseId: e.target.value })}
            options={[{ value: '', label: 'Select…' }, ...exerciseOptions]}
            required
          />
          <div className="grid gap-3 sm:grid-cols-2">
            <SelectField
              label="Node type"
              value={form.nodeType}
              onChange={(e) => setForm({ ...form, nodeType: e.target.value })}
              options={[...NODE_TYPE]}
            />
            <SelectField
              label="Difficulty"
              value={form.difficulty}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
              options={[...DIFFICULTY]}
            />
            <SelectField
              label="Operator"
              value={form.operator}
              onChange={(e) => setForm({ ...form, operator: e.target.value })}
              options={[...OPERATOR]}
            />
            <Input
              label="Target value"
              type="number"
              step="0.01"
              value={form.targetValue}
              onChange={(e) =>
                setForm({ ...form, targetValue: Number(e.target.value) })
              }
              required
            />
            <Input
              label="Unit label"
              value={form.unitLabel}
              onChange={(e) => setForm({ ...form, unitLabel: e.target.value })}
              required
              placeholder="seconds / reps"
            />
            <SelectField
              label="Status"
              value={form.status ?? 'ACTIVE'}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
              options={[...CATALOG_STATUS]}
            />
            <Input
              label="XP reward"
              type="number"
              value={form.xpReward ?? ''}
              onChange={(e) =>
                setForm({
                  ...form,
                  xpReward: e.target.value === '' ? null : Number(e.target.value),
                })
              }
            />
            <Input
              label="Estimated minutes"
              type="number"
              value={form.estimatedMinutes ?? ''}
              onChange={(e) =>
                setForm({
                  ...form,
                  estimatedMinutes:
                    e.target.value === '' ? null : Number(e.target.value),
                })
              }
            />
          </div>
          <div className="flex gap-2 pt-2">
            <Button type="submit" loading={create.isPending || update.isPending}>
              {editingId ? 'Save' : 'Create'}
            </Button>
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setShowForm(false)
                setEditingId(null)
              }}
            >
              Cancel
            </Button>
          </div>
        </form>
      ) : null}

      {nodes.isLoading ? (
        <Spinner />
      ) : (
        <ul className="space-y-2">
          {(nodes.data ?? []).map((node) => (
            <li
              key={node.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-stone-200 bg-stone-50/90 px-4 py-3 shadow-sm"
            >
              <div>
                <p className="font-semibold text-stone-900">{node.name}</p>
                <p className="text-xs text-stone-500">
                  Exercise: {node.exercise.name} · {node.operator} {node.targetValue}{' '}
                  {node.unitLabel} · {node.difficulty} · {node.status}
                </p>
              </div>
              <div className="flex gap-2">
                <Button type="button" variant="secondary" onClick={() => startEdit(node)}>
                  Edit
                </Button>
                {node.status !== 'DEPRECATED' ? (
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={async () => {
                      try {
                        await deprecate.mutateAsync(node.id)
                        toast.success('Goal deprecated')
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
          {!nodes.data?.length ? (
            <p className="text-sm text-stone-500">No goals yet.</p>
          ) : null}
        </ul>
      )}
    </div>
  )
}
