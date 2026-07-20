import { Link } from 'react-router'
import type { OnboardingAnswersResponse } from '@/shared/api/types'
import { PageShell } from '@/shared/ui/PageShell'

function readResult(): OnboardingAnswersResponse | null {
  try {
    const raw = sessionStorage.getItem('calistrack.lastPlacement')
    if (!raw) return null
    return JSON.parse(raw) as OnboardingAnswersResponse
  } catch {
    return null
  }
}

export function PlacementResultPage() {
  const result = readResult()

  if (!result) {
    return (
      <PageShell title="Placement result">
        <p className="text-stone-600">No placement result found.</p>
        <Link
          to="/home"
          className="mt-4 inline-flex rounded-lg bg-emerald-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-800"
        >
          Go to home
        </Link>
      </PageShell>
    )
  }

  return (
    <PageShell
      title="You're placed"
      subtitle="Your first workout session is ready as PENDING."
    >
      <div className="space-y-4 rounded-2xl border border-stone-200 bg-white/90 p-6 shadow-sm">
        <dl className="grid gap-3 text-sm sm:grid-cols-2">
          <div>
            <dt className="text-stone-500">Focus node</dt>
            <dd className="font-mono text-stone-900">{result.focusNodeId}</dd>
          </div>
          <div>
            <dt className="text-stone-500">Session</dt>
            <dd className="font-mono text-stone-900">{result.sessionId}</dd>
          </div>
          <div>
            <dt className="text-stone-500">Workout</dt>
            <dd className="font-semibold text-stone-900">{result.workoutTitle}</dd>
          </div>
          <div>
            <dt className="text-stone-500">Status</dt>
            <dd className="font-semibold text-emerald-800">{result.sessionStatus}</dd>
          </div>
        </dl>

        <div>
          <h2 className="mb-2 text-sm font-semibold text-stone-800">Path nodes</h2>
          <ul className="space-y-1 text-sm">
            {result.placedNodes.map((n) => (
              <li
                key={n.nodeId}
                className="flex items-center justify-between rounded-lg bg-stone-50 px-3 py-2"
              >
                <span className="font-mono text-xs text-stone-600">
                  …{n.nodeId.slice(-4)}
                </span>
                <span className="font-medium text-stone-800">{n.status}</span>
              </li>
            ))}
          </ul>
        </div>

        <Link
          to="/home"
          className="inline-flex items-center justify-center rounded-lg bg-emerald-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-emerald-800"
        >
          Continue to home
        </Link>
      </div>
    </PageShell>
  )
}
