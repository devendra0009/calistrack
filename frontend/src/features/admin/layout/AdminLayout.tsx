import { startTransition } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import { toast } from 'sonner'
import { ADMIN_STEPS } from '@/features/admin/constants'
import { logout } from '@/features/auth/api'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { useMe } from '@/features/profile/api'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/Button'
import { ThemeToggle } from '@/shared/ui/ThemeToggle'

export function AdminLayout() {
  const me = useMe()
  const navigate = useNavigate()
  const { signOutLocal } = useAuthSession()

  return (
    <div className="bg-app min-h-dvh">
      <header className="border-b border-stone-200/80 bg-stone-50/70 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-4">
          <div className="flex items-center gap-3">
            <Link to="/admin" className="text-lg font-bold tracking-tight text-emerald-900">
              CalisTrack Admin
            </Link>
            <span className="hidden rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-900 sm:inline">
              {me.data?.displayName ?? 'Admin'}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <ThemeToggle />
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
        </div>
      </header>

      <div className="mx-auto grid max-w-5xl gap-6 px-4 py-6 lg:grid-cols-[220px_1fr]">
        <aside className="space-y-1 lg:sticky lg:top-4 lg:self-start">
          <NavLink
            to="/admin"
            end
            className={({ isActive }) =>
              cn(
                'block rounded-lg px-3 py-2 text-sm font-medium',
                isActive
                  ? 'bg-emerald-800 text-white'
                  : 'text-stone-700 hover:bg-stone-100',
              )
            }
          >
            Overview
          </NavLink>
          {ADMIN_STEPS.map((s) => (
            <NavLink
              key={s.to}
              to={s.to}
              className={({ isActive }) =>
                cn(
                  'block rounded-lg px-3 py-2 text-sm font-medium',
                  isActive
                    ? 'bg-emerald-800 text-white'
                    : 'text-stone-700 hover:bg-stone-100',
                )
              }
            >
              <span className="text-xs opacity-70">{s.step}.</span> {s.title}
            </NavLink>
          ))}
        </aside>
        <main className="min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
