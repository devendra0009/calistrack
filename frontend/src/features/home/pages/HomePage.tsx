import { Link, useNavigate } from 'react-router'
import { startTransition } from 'react'
import { toast } from 'sonner'
import { Button } from '@/shared/ui/Button'
import { PageShell } from '@/shared/ui/PageShell'
import { Spinner } from '@/shared/ui/Spinner'
import { logout } from '@/features/auth/api'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { useMe } from '@/features/profile/api'
import {
  isSessionNotFound,
  useCurrentWorkoutSession,
} from '@/features/home/api'

export function HomePage() {
  const me = useMe()
  const session = useCurrentWorkoutSession(Boolean(me.data))
  const navigate = useNavigate()
  const { signOutLocal } = useAuthSession()

  if (me.isLoading || session.isLoading) {
    return (
      <PageShell title="Home">
        <Spinner />
      </PageShell>
    )
  }

  const user = me.data
  const current = session.data
  const missingSession = session.isError && isSessionNotFound(session.error)

  return (
    <PageShell
      title={`Hey, ${user?.displayName ?? 'athlete'}`}
      subtitle="Your setup is done. Train loop comes next."
      actions={
        <div className="flex items-center gap-2">
          <Link
            to="/profile"
            className="rounded-lg px-3 py-2 text-sm font-medium text-stone-700 hover:bg-stone-100"
          >
            Profile
          </Link>
          <Button
            variant="secondary"
            onClick={async () => {
              try {
                await logout()
              } finally {
                signOutLocal()
                toast.success('Signed out')
                startTransition(() => navigate('/login', { replace: true }))
              }
            }}
          >
            Sign out
          </Button>
        </div>
      }
    >
      <div className="space-y-4">
        <section className="rounded-2xl border border-stone-200 bg-white/90 p-6 shadow-sm">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-stone-500">
            Goal
          </h2>
          <p className="mt-2 text-xl font-bold text-stone-900">
            {user?.goal?.name ?? 'No goal set'}
          </p>
          {user?.goal?.description ? (
            <p className="mt-1 text-sm text-stone-600">{user.goal.description}</p>
          ) : null}
        </section>

        {current ? (
          <section className="rounded-2xl border border-emerald-200 bg-emerald-50/80 p-6">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-emerald-800">
              Current session
            </h2>
            <p className="mt-2 text-lg font-bold text-emerald-950">
              {current.workoutTitle}
            </p>
            {current.workoutDescription ? (
              <p className="mt-1 text-sm text-emerald-900">
                {current.workoutDescription}
              </p>
            ) : null}
            <p className="mt-2 text-sm text-emerald-900">
              Focus: {current.focusNodeName}
            </p>
            <p className="mt-1 text-sm text-emerald-900">
              Status: {current.status}
              {current.verified ? ' · verified' : ''}
            </p>
            <Button
              className="mt-4"
              onClick={() =>
                startTransition(() =>
                  navigate(`/sessions/${current.sessionId}`),
                )
              }
            >
              {current.status === 'PENDING' ? 'Start training' : 'Continue session'}
            </Button>
          </section>
        ) : (
          <section className="rounded-2xl border border-stone-200 bg-white/90 p-6 shadow-sm">
            <p className="text-sm text-stone-600">
              {missingSession
                ? 'No workout session yet. Complete placement questions to get your first session.'
                : 'Could not load your current workout session.'}
            </p>
          </section>
        )}
      </div>
    </PageShell>
  )
}
