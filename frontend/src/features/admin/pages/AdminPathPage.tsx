import { useMemo, useState } from 'react'
import { toast } from 'sonner'
import {
  useAdminEdges,
  useAdminNodes,
  useCreateEdge,
  useDeleteEdge,
} from '@/features/admin/api'
import { cn } from '@/shared/lib/cn'
import type { AdminNodeEdgeResponse, NamedRef } from '@/shared/api/types'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui/Button'
import { SelectField } from '@/shared/ui/SelectField'
import { Spinner } from '@/shared/ui/Spinner'

type ViewMode = 'edges' | 'paths'

type PathTreeNode = {
  id: string
  name: string
  children: PathTreeNode[]
}

/** Prerequisites of a node: edges where this node is `to` (unlock target). */
function buildPrereqMap(edges: AdminNodeEdgeResponse[]) {
  const prereqs = new Map<string, NamedRef[]>()
  for (const edge of edges) {
    const list = prereqs.get(edge.toNode.id) ?? []
    list.push(edge.fromNode)
    prereqs.set(edge.toNode.id, list)
  }
  return prereqs
}

/**
 * Topmost / goal nodes: appear as unlock targets and never as a prerequisite
 * of something else (out-degree 0 in the progression graph).
 */
function findGoalNodes(
  edges: AdminNodeEdgeResponse[],
  nameById: Map<string, string>,
): NamedRef[] {
  const asFrom = new Set(edges.map((e) => e.fromNode.id))
  const asTo = new Set(edges.map((e) => e.toNode.id))
  const goals: NamedRef[] = []
  for (const id of asTo) {
    if (!asFrom.has(id)) {
      goals.push({ id, name: nameById.get(id) ?? id })
    }
  }
  return goals.sort((a, b) => a.name.localeCompare(b.name))
}

function buildPathTree(
  goalId: string,
  prereqs: Map<string, NamedRef[]>,
  nameById: Map<string, string>,
  visiting = new Set<string>(),
): PathTreeNode {
  if (visiting.has(goalId)) {
    return {
      id: goalId,
      name: `${nameById.get(goalId) ?? goalId} (cycle)`,
      children: [],
    }
  }
  visiting.add(goalId)
  const children = (prereqs.get(goalId) ?? [])
    .slice()
    .sort((a, b) => a.name.localeCompare(b.name))
    .map((p) =>
      buildPathTree(p.id, prereqs, nameById, new Set(visiting)),
    )
  visiting.delete(goalId)
  return {
    id: goalId,
    name: nameById.get(goalId) ?? goalId,
    children,
  }
}

/** Flatten a linear spine for chain display; null if the tree branches. */
function linearChain(tree: PathTreeNode): string[] | null {
  const names = [tree.name]
  let node = tree
  while (node.children.length === 1) {
    node = node.children[0]
    names.push(node.name)
  }
  if (node.children.length > 1) return null
  return names
}

function PathChain({ names }: { names: string[] }) {
  return (
    <p className="flex flex-wrap items-center gap-x-1 gap-y-1 text-sm text-stone-800">
      {names.map((name, i) => (
        <span key={`${name}-${i}`} className="inline-flex items-center gap-1">
          {i > 0 ? (
            <span className="mx-1 text-stone-400" aria-hidden>
              →
            </span>
          ) : null}
          <span
            className={cn(
              i === 0 && 'font-bold text-emerald-900',
              i === names.length - 1 && i > 0 && 'font-semibold text-stone-700',
              i > 0 && i < names.length - 1 && 'font-medium',
            )}
          >
            {name}
          </span>
        </span>
      ))}
    </p>
  )
}

function PathTreeList({
  node,
  depth = 0,
}: {
  node: PathTreeNode
  depth?: number
}) {
  return (
    <li className={cn(depth > 0 && 'mt-1')}>
      <div
        className="flex items-start gap-2 text-sm text-stone-800"
        style={{ paddingLeft: depth * 16 }}
      >
        {depth > 0 ? (
          <span className="shrink-0 text-stone-400" aria-hidden>
            →
          </span>
        ) : null}
        <span
          className={cn(
            depth === 0 && 'font-bold text-emerald-900',
            depth > 0 && 'font-medium',
          )}
        >
          {node.name}
        </span>
        {depth === 0 ? (
          <span className="text-xs font-normal text-stone-500">(goal)</span>
        ) : null}
        {node.children.length === 0 && depth > 0 ? (
          <span className="text-xs font-normal text-stone-500">(basic)</span>
        ) : null}
      </div>
      {node.children.length > 0 ? (
        <ul>
          {node.children.map((child) => (
            <PathTreeList key={child.id} node={child} depth={depth + 1} />
          ))}
        </ul>
      ) : null}
    </li>
  )
}

function PathView({
  edges,
  nameById,
}: {
  edges: AdminNodeEdgeResponse[]
  nameById: Map<string, string>
}) {
  const [selectedGoalId, setSelectedGoalId] = useState('')

  const prereqs = useMemo(() => buildPrereqMap(edges), [edges])
  const goals = useMemo(
    () => findGoalNodes(edges, nameById),
    [edges, nameById],
  )

  const goalOptions = [
    { value: '', label: 'All topmost goals' },
    ...goals.map((g) => ({ value: g.id, label: g.name })),
  ]

  const visibleGoals = selectedGoalId
    ? goals.filter((g) => g.id === selectedGoalId)
    : goals

  if (!edges.length) {
    return (
      <p className="text-sm text-stone-500">
        No edges yet — switch to Edges view to connect goals.
      </p>
    )
  }

  if (!goals.length) {
    return (
      <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
        No topmost goal found. Every connected node still unlocks something else
        (possible cycle). Check the Edges view.
      </p>
    )
  }

  return (
    <div className="space-y-4">
      <SelectField
        label="Topmost goal"
        value={selectedGoalId}
        onChange={(e) => setSelectedGoalId(e.target.value)}
        options={goalOptions}
      />
      <p className="text-xs text-stone-500">
        Each path reads goal → prerequisite → … → basic (foundation) node.
      </p>
      <ul className="space-y-3">
        {visibleGoals.map((goal) => {
          const tree = buildPathTree(goal.id, prereqs, nameById)
          const chain = linearChain(tree)
          return (
            <li
              key={goal.id}
              className="rounded-2xl border border-stone-200 bg-stone-50/90 px-4 py-4 shadow-sm"
            >
              {chain ? (
                <PathChain names={chain} />
              ) : (
                <ul>
                  <PathTreeList node={tree} />
                </ul>
              )}
            </li>
          )
        })}
      </ul>
    </div>
  )
}

export function AdminPathPage() {
  const nodes = useAdminNodes('ACTIVE')
  const edges = useAdminEdges()
  const create = useCreateEdge()
  const remove = useDeleteEdge()

  const [view, setView] = useState<ViewMode>('edges')
  const [fromNodeId, setFromNodeId] = useState('')
  const [toNodeId, setToNodeId] = useState('')

  const options = [
    { value: '', label: 'Select…' },
    ...(nodes.data ?? []).map((n) => ({ value: n.id, label: n.name })),
  ]

  const nameById = useMemo(() => {
    const map = new Map<string, string>()
    for (const n of nodes.data ?? []) map.set(n.id, n.name)
    for (const e of edges.data ?? []) {
      map.set(e.fromNode.id, e.fromNode.name)
      map.set(e.toNode.id, e.toNode.name)
    }
    return map
  }, [nodes.data, edges.data])

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs font-semibold uppercase tracking-wide text-emerald-900">
          Step 4
        </p>
        <h1 className="text-2xl font-bold text-stone-900">Path sequence</h1>
        <p className="mt-1 max-w-2xl text-sm text-stone-600">
          Connect goals so learners progress step by step.{' '}
          <span className="font-medium text-stone-800">From</span> is the
          prerequisite of <span className="font-medium text-stone-800">To</span>
          (e.g. Tuck Planche → Planche).
        </p>
      </header>

      <div
        className="inline-flex rounded-xl border border-stone-200 bg-stone-100/80 p-1"
        role="tablist"
        aria-label="Path views"
      >
        <button
          type="button"
          role="tab"
          aria-selected={view === 'edges'}
          className={cn(
            'rounded-lg px-4 py-2 text-sm font-semibold transition',
            view === 'edges'
              ? 'bg-stone-50 text-stone-900 shadow-sm'
              : 'text-stone-600 hover:text-stone-900',
          )}
          onClick={() => setView('edges')}
        >
          Edges (from → to)
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={view === 'paths'}
          className={cn(
            'rounded-lg px-4 py-2 text-sm font-semibold transition',
            view === 'paths'
              ? 'bg-stone-50 text-stone-900 shadow-sm'
              : 'text-stone-600 hover:text-stone-900',
          )}
          onClick={() => setView('paths')}
        >
          Paths (goal → basic)
        </button>
      </div>

      {(nodes.data?.length ?? 0) < 2 && !nodes.isLoading ? (
        <p className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          You need at least two goals before you can wire a path.
        </p>
      ) : null}

      {view === 'edges' ? (
        <>
          <form
            className="space-y-3 rounded-2xl border border-stone-200 bg-stone-50/90 p-5 shadow-sm"
            onSubmit={async (e) => {
              e.preventDefault()
              if (!fromNodeId || !toNodeId) {
                toast.error('Pick both nodes')
                return
              }
              if (fromNodeId === toNodeId) {
                toast.error('From and To must be different goals')
                return
              }
              try {
                await create.mutateAsync({ fromNodeId, toNodeId })
                toast.success('Edge created')
                setFromNodeId('')
                setToNodeId('')
              } catch (err) {
                toast.error(
                  err instanceof ApiError ? err.message : 'Create failed',
                )
              }
            }}
          >
            <h2 className="font-semibold text-stone-900">
              Add prerequisite edge
            </h2>
            <div className="grid gap-3 sm:grid-cols-2">
              <SelectField
                label="From (prerequisite)"
                value={fromNodeId}
                onChange={(e) => setFromNodeId(e.target.value)}
                options={options}
              />
              <SelectField
                label="To (unlocks this)"
                value={toNodeId}
                onChange={(e) => setToNodeId(e.target.value)}
                options={options}
              />
            </div>
            <Button type="submit" loading={create.isPending}>
              Connect goals
            </Button>
          </form>

          {edges.isLoading || nodes.isLoading ? (
            <Spinner />
          ) : (
            <ul className="space-y-2">
              {(edges.data ?? []).map((edge) => (
                <li
                  key={edge.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-stone-200 bg-stone-50/90 px-4 py-3 shadow-sm"
                >
                  <p className="text-sm text-stone-800">
                    <span className="font-semibold">{edge.fromNode.name}</span>
                    <span className="mx-2 text-stone-400">→</span>
                    <span className="font-semibold">{edge.toNode.name}</span>
                    <span className="ml-2 text-xs text-stone-500">
                      ({edge.relationType})
                    </span>
                  </p>
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={async () => {
                      try {
                        await remove.mutateAsync(edge.id)
                        toast.success('Edge removed')
                      } catch (err) {
                        toast.error(
                          err instanceof ApiError
                            ? err.message
                            : 'Delete failed',
                        )
                      }
                    }}
                  >
                    Remove
                  </Button>
                </li>
              ))}
              {!edges.data?.length ? (
                <p className="text-sm text-stone-500">
                  No edges yet — connect two goals.
                </p>
              ) : null}
            </ul>
          )}
        </>
      ) : edges.isLoading || nodes.isLoading ? (
        <Spinner />
      ) : (
        <PathView edges={edges.data ?? []} nameById={nameById} />
      )}
    </div>
  )
}
