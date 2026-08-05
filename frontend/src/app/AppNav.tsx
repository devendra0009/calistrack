import { Link, NavLink, useNavigate } from 'react-router'
import { startTransition } from 'react'
import {
  ClipboardCheck,
  Home,
  LogOut,
  PersonStanding,
  User,
} from 'lucide-react'
import { toast } from '@/shared/ui/notify'
import { cn } from '@/shared/lib/cn'
import { ThemeToggle } from '@/shared/ui/ThemeToggle'
import { logout } from '@/features/auth/api'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'
import { usePrefetchAssessmentPath } from '@/features/assessment/api'
import { usePrefetchStretchingToday } from '@/features/stretching/api'
import { WorkoutMusicControl } from '@/features/workout-music/WorkoutMusicControl'

const LINKS = [
  { to: '/home', label: 'Home', end: true, icon: Home },
  { to: '/stretch', label: 'Stretch', end: true, icon: PersonStanding },
  { to: '/assessment', label: 'Assess', end: true, icon: ClipboardCheck },
  { to: '/profile', label: 'Profile', end: true, icon: User },
] as const

export function AppNav() {
  const navigate = useNavigate()
  const { signOutLocal } = useAuthSession()
  const prefetchAssessment = usePrefetchAssessmentPath()
  const prefetchStretch = usePrefetchStretchingToday()

  const prefetchFor = (to: (typeof LINKS)[number]['to']) => {
    if (to === '/assessment') return prefetchAssessment
    if (to === '/stretch') return prefetchStretch
    return undefined
  }

  const signOut = async () => {
    try {
      await logout()
    } finally {
      signOutLocal()
      toast.success('Signed out')
      startTransition(() => navigate('/login', { replace: true }))
    }
  }

  return (
    <>
      <header className="sticky top-0 z-20 border-b border-stone-200/80 bg-stone-50/90 backdrop-blur supports-backdrop-filter:bg-stone-50/75">
        <div className="mx-auto flex max-w-3xl items-center justify-between gap-3 px-3 py-2.5 sm:px-4 sm:py-3">
          <Link
            to="/home"
            className="shrink-0 text-base font-bold tracking-tight text-emerald-900 sm:text-lg"
          >
            CalisTrack
          </Link>

          {/* Desktop / tablet inline nav */}
          <nav
            className="hidden min-w-0 items-center justify-end gap-1 md:flex md:gap-2"
            aria-label="App"
          >
            {LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                onMouseEnter={prefetchFor(link.to)}
                onFocus={prefetchFor(link.to)}
                className={({ isActive }) =>
                  cn(
                    'rounded-lg px-3 py-2 text-sm font-medium transition',
                    isActive
                      ? 'bg-emerald-50 text-emerald-900'
                      : 'text-stone-700 hover:bg-stone-100',
                  )
                }
              >
                {link.label}
              </NavLink>
            ))}
            <WorkoutMusicControl className="ml-1" />
            <ThemeToggle />
            <button
              type="button"
              onClick={() => void signOut()}
              className="inline-flex items-center gap-1.5 rounded-lg border border-stone-300 bg-stone-50 px-3 py-2 text-sm font-semibold text-stone-900 transition hover:bg-stone-100"
            >
              <LogOut className="size-3.5" aria-hidden />
              Sign out
            </button>
          </nav>

          {/* Mobile top actions */}
          <div className="flex items-center gap-1.5 md:hidden">
            <WorkoutMusicControl />
            <ThemeToggle />
            <button
              type="button"
              onClick={() => void signOut()}
              className="inline-flex size-10 items-center justify-center rounded-lg border border-stone-300 bg-stone-50 text-stone-700 transition hover:bg-stone-100"
              aria-label="Sign out"
              title="Sign out"
            >
              <LogOut className="size-4" aria-hidden />
            </button>
          </div>
        </div>
      </header>

      {/* Mobile bottom tab bar */}
      <nav
        className="fixed inset-x-0 bottom-0 z-30 border-t border-stone-200/80 bg-stone-50/95 pb-[env(safe-area-inset-bottom)] backdrop-blur supports-backdrop-filter:bg-stone-50/85 md:hidden"
        aria-label="Primary"
      >
        <div className="mx-auto grid max-w-3xl grid-cols-4">
          {LINKS.map((link) => {
            const Icon = link.icon
            const prefetch = prefetchFor(link.to)
            return (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                onTouchStart={prefetch}
                onMouseEnter={prefetch}
                className={({ isActive }) =>
                  cn(
                    'flex min-h-14 flex-col items-center justify-center gap-0.5 px-1 pt-1.5 text-[11px] font-semibold transition',
                    isActive
                      ? 'text-emerald-800'
                      : 'text-stone-500 active:text-stone-800',
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    <span
                      className={cn(
                        'inline-flex size-8 items-center justify-center rounded-xl',
                        isActive && 'bg-emerald-100 text-emerald-900',
                      )}
                    >
                      <Icon className="size-5" aria-hidden strokeWidth={isActive ? 2.25 : 2} />
                    </span>
                    {link.label}
                  </>
                )}
              </NavLink>
            )
          })}
        </div>
      </nav>
    </>
  )
}
