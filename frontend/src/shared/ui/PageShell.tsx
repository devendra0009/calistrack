import type { ReactNode } from 'react'
import { Link } from 'react-router'

export function PageShell({
  title,
  subtitle,
  children,
  actions,
}: {
  title: string
  subtitle?: string
  children: ReactNode
  actions?: ReactNode
}) {
  return (
    <div className="min-h-dvh bg-[radial-gradient(ellipse_at_top,_#ecfdf5_0%,_#fafaf9_45%,_#f5f5f4_100%)]">
      <header className="border-b border-stone-200/80 bg-white/70 backdrop-blur">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-4">
          <Link to="/" className="text-lg font-bold tracking-tight text-emerald-900">
            CalisTrack
          </Link>
          {actions}
        </div>
      </header>
      <main className="mx-auto max-w-3xl px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight text-stone-900">{title}</h1>
          {subtitle ? (
            <p className="mt-2 max-w-xl text-stone-600">{subtitle}</p>
          ) : null}
        </div>
        {children}
      </main>
    </div>
  )
}
