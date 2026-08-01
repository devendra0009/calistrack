import { Link, NavLink, useNavigate } from 'react-router'
import { startTransition } from 'react'
import { toast } from 'sonner'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/Button'
import { ThemeToggle } from '@/shared/ui/ThemeToggle'
import { logout } from '@/features/auth/api'
import { useAuthSession } from '@/features/auth/AuthSessionProvider'

const LINKS = [
  { to: '/home', label: 'Home', end: true },
  { to: '/stretch', label: 'Stretch', end: true },
  { to: '/assessment', label: 'Assess', end: true },
  { to: '/profile', label: 'Profile', end: true },
] as const

export function AppNav() {
  const navigate = useNavigate()
  const { signOutLocal } = useAuthSession()

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
    <header className="sticky top-0 z-20 border-b border-stone-200/80 bg-stone-50/80 backdrop-blur">
      <div className="mx-auto flex max-w-3xl items-center justify-between gap-3 px-4 py-3">
        <Link
          to="/home"
          className="shrink-0 text-lg font-bold tracking-tight text-emerald-900"
        >
          CalisTrack
        </Link>

        <nav
          className="flex min-w-0 items-center justify-end gap-1 sm:gap-2"
          aria-label="App"
        >
          {LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                cn(
                  'rounded-lg px-2.5 py-2 text-sm font-medium transition sm:px-3',
                  isActive
                    ? 'bg-emerald-50 text-emerald-900'
                    : 'text-stone-700 hover:bg-stone-100',
                )
              }
            >
              {link.label}
            </NavLink>
          ))}
          <ThemeToggle className="ml-1" />
          <Button variant="secondary" onClick={signOut}>
            Sign out
          </Button>
        </nav>
      </div>
    </header>
  )
}
