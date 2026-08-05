import { useMemo, useState, type FormEvent } from 'react'
import { toast } from '@/shared/ui/notify'
import {
  useAdminEdges,
  useAdminNodes,
  useAdminPathQuestions,
  useCreatePathQuestion,
  useDeletePathQuestion,
  useUpdatePathQuestion,
} from '@/features/admin/api'
import type {
  AdminPathQuestionRequest,
  AdminPathQuestionResponse,
  PlacementAnswerType,
} from '@/shared/api/types'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { Input } from '@/shared/ui/Input'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

const ANSWER_TYPES: PlacementAnswerType[] = ['YES_NO', 'REPS']

export function AdminPathQuestionsPage() {
  const nodes = useAdminNodes('ACTIVE')
  const edges = useAdminEdges()
  const [goalNodeId, setGoalNodeId] = useState('')
  const questions = useAdminPathQuestions(goalNodeId || undefined)
  const create = useCreatePathQuestion()
  const update = useUpdatePathQuestion()
  const remove = useDeletePathQuestion()

  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [form, setForm] = useState<AdminPathQuestionRequest>({
    goalNodeId: '',
    nodeId: '',
    prompt: '',
    answerType: 'YES_NO',
    sortOrder: 1,
  })

  /** Nodes that touch this goal via edges (prereqs + goal itself), for the question target picker. */
  const pathNodeOptions = useMemo(() => {
    if (!goalNodeId || !edges.data) return []
    const connected = new Set<string>()
    connected.add(goalNodeId)
    for (const e of edges.data) {
      if (e.toNode.id === goalNodeId || e.fromNode.id === goalNodeId) {
        connected.add(e.fromNode.id)
        connected.add(e.toNode.id)
      }
      // Also include transitive-ish: any edge where from or to is already connected (1-pass expand)
    }
    // Expand once along all edges that share ids with current connected set
    let grew = true
    while (grew) {
      grew = false
      for (const e of edges.data) {
        const touch =
          connected.has(e.fromNode.id) || connected.has(e.toNode.id)
        if (touch) {
          if (!connected.has(e.fromNode.id) || !connected.has(e.toNode.id)) {
            connected.add(e.fromNode.id)
            connected.add(e.toNode.id)
            grew = true
          }
        }
      }
    }
    return (nodes.data ?? [])
      .filter((n) => connected.has(n.id))
      .map((n) => ({ value: n.id, label: n.name }))
  }, [goalNodeId, edges.data, nodes.data])

  function startCreate() {
    if (!goalNodeId) {
      toast.error('Pick a goal first')
      return
    }
    const nextSort =
      Math.max(0, ...(questions.data ?? []).map((q) => q.sortOrder)) + 1
    setEditingId(null)
    setForm({
      goalNodeId,
      nodeId: pathNodeOptions[0]?.value ?? '',
      prompt: '',
      answerType: 'YES_NO',
      sortOrder: nextSort,
    })
    setShowForm(true)
  }

  function startEdit(q: AdminPathQuestionResponse) {
    setEditingId(q.id)
    setForm({
      goalNodeId: q.goalNode.id,
      nodeId: q.node.id,
      prompt: q.prompt,
      answerType: q.answerType,
      sortOrder: q.sortOrder,
    })
    setShowForm(true)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!form.goalNodeId || !form.nodeId || !form.prompt.trim()) {
      toast.error('Goal, path node, and prompt are required')
      return
    }
    try {
      if (editingId) {
        await update.mutateAsync({ id: editingId, body: form })
        toast.success('Question updated')
      } else {
        await create.mutateAsync(form)
        toast.success('Question created')
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
            Step 5
          </p>
          <h1 className="text-2xl font-bold text-stone-900">Placement questions</h1>
          <p className="mt-1 max-w-2xl text-sm text-stone-600">
            For each goal, define ordered questions about nodes on that path.
            Answers decide where to place the user (first failed answer = focus
            node). Use YES_NO or REPS (vs the node&apos;s target value).
          </p>
        </div>
        <Button type="button" onClick={startCreate} disabled={!goalNodeId}>
          New question
        </Button>
      </header>

      <SelectField
        label="Goal / node"
        value={goalNodeId}
        onChange={(e) => {
          setGoalNodeId(e.target.value)
          setShowForm(false)
          setEditingId(null)
        }}
        options={[
          { value: '', label: 'Select a goal…' },
          ...(nodes.data ?? []).map((n) => ({ value: n.id, label: n.name })),
        ]}
      />

      {!goalNodeId ? (
        <p className="text-sm text-stone-500">
          Select a goal to view and edit its placement questionnaire.
        </p>
      ) : null}

      {goalNodeId && pathNodeOptions.length < 1 && !edges.isLoading ? (
        <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          This goal has no path edges yet. Complete Step 4 so questions can target
          nodes on the path.
        </p>
      ) : null}

      {showForm ? (
        <form
          onSubmit={onSubmit}
          className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm"
        >
          <h2 className="font-semibold text-stone-900">
            {editingId ? 'Edit question' : 'Create question'}
          </h2>
          <SelectField
            label="Path node this question tests"
            value={form.nodeId}
            onChange={(e) => setForm({ ...form, nodeId: e.target.value })}
            options={[
              { value: '', label: 'Select…' },
              ...pathNodeOptions,
            ]}
            required
          />
          <label className="flex flex-col gap-1.5 text-sm">
            <span className="font-medium text-stone-800">Prompt</span>
            <textarea
              className="min-h-20 rounded-lg border border-stone-300 bg-stone-50 px-3 py-2 outline-none ring-emerald-600/30 focus:ring-2"
              value={form.prompt}
              onChange={(e) => setForm({ ...form, prompt: e.target.value })}
              placeholder="e.g. Can you hold a hollow body for 30 seconds?"
              required
            />
          </label>
          <div className="grid gap-3 sm:grid-cols-2">
            <SelectField
              label="Answer type"
              value={form.answerType}
              onChange={(e) =>
                setForm({
                  ...form,
                  answerType: e.target.value as PlacementAnswerType,
                })
              }
              options={ANSWER_TYPES}
            />
            <Input
              label="Sort order"
              type="number"
              min={1}
              value={form.sortOrder}
              onChange={(e) =>
                setForm({ ...form, sortOrder: Number(e.target.value) })
              }
              required
            />
          </div>
          <p className="text-xs text-stone-500">
            Sort order = ask order. Placement walks questions in order; first fail
            becomes the user&apos;s starting node.
          </p>
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

      {goalNodeId && questions.isLoading ? (
        <Spinner />
      ) : goalNodeId ? (
        <ul className="space-y-2">
          {(questions.data ?? []).map((q) => (
            <li
              key={q.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-stone-200 bg-stone-50/90 px-4 py-3 shadow-sm"
            >
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-stone-500">
                  #{q.sortOrder} · {q.answerType} · tests {q.node.name}
                </p>
                <p className="mt-1 font-medium text-stone-900">{q.prompt}</p>
              </div>
              <div className="flex gap-2">
                <Button type="button" variant="secondary" onClick={() => startEdit(q)}>
                  Edit
                </Button>
                <Button
                  type="button"
                  variant="ghost"
                  onClick={async () => {
                    try {
                      await remove.mutateAsync(q.id)
                      toast.success('Question deleted')
                    } catch (err) {
                      toast.error(
                        err instanceof ApiError ? err.message : 'Delete failed',
                      )
                    }
                  }}
                >
                  Delete
                </Button>
              </div>
            </li>
          ))}
          {!questions.data?.length ? (
            <p className="text-sm text-stone-500">
              No questions for this goal yet — users cannot complete onboarding
              until at least one exists.
            </p>
          ) : null}
        </ul>
      ) : null}
    </div>
  )
}
