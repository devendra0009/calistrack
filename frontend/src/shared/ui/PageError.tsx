import type { ReactNode } from 'react'
import { OctagonAlert, RotateCcw } from 'lucide-react'
import { cn } from '@/shared/lib/cn'
import { Button } from '@/shared/ui/Button'

/** Persistent page-level error (not a toast) — workout-styled recovery card. */
export function PageError({
  title = 'Couldn’t finish this set',
  message,
  onRetry,
  className,
  children,
}: {
  title?: string
  message: string
  onRetry?: () => void
  className?: string
  children?: ReactNode
}) {
  return (
    <div
      role="alert"
      className={cn(
        'relative overflow-hidden rounded-2xl border border-orange-200/90 bg-stone-50/95 p-5 shadow-sm sm:p-6',
        className,
      )}
    >
      <div
        className="pointer-events-none absolute inset-y-0 left-0 w-1.5 bg-linear-to-b from-orange-500 to-amber-600"
        aria-hidden
      />
      <div className="flex gap-3 pl-2">
        <div className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-orange-100 text-orange-800">
          <OctagonAlert className="size-5" aria-hidden />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-base font-bold tracking-tight text-stone-900">{title}</p>
          <p className="mt-1 text-sm leading-relaxed text-stone-600">{message}</p>
          {children}
          {onRetry ? (
            <Button
              type="button"
              variant="secondary"
              className="mt-4"
              onClick={onRetry}
            >
              <RotateCcw className="size-3.5" aria-hidden />
              Try again
            </Button>
          ) : null}
        </div>
      </div>
    </div>
  )
}
