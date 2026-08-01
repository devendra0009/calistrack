import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { ThemeToggle } from '@/shared/ui/ThemeToggle'

export function PageShell({
  title,
  subtitle,
  children,
  actions,
  embedded = false,
}: {
  title: string
  subtitle?: string
  children: ReactNode
  actions?: ReactNode
  /** When true, skip outer chrome/header (used inside AppLayout). */
  embedded?: boolean
}) {
  const body = (
    <main className="mx-auto max-w-3xl px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-stone-900">
          {title}
        </h1>
        {subtitle ? (
          <p className="mt-2 max-w-xl text-stone-600">{subtitle}</p>
        ) : null}
      </div>
      {children}
    </main>
  )

  if (embedded) {
    return body
  }

  return (
    <div className="bg-app min-h-dvh">
      <header className="border-b border-stone-200/80 bg-stone-50/70 backdrop-blur">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-4">
          <Link
            to="/"
            className="text-lg font-bold tracking-tight text-emerald-900"
          >
            CalisTrack
          </Link>
          <div className="flex items-center gap-2">
            {actions}
            <ThemeToggle />
          </div>
        </div>
      </header>
      {body}
    </div>
  )
}
