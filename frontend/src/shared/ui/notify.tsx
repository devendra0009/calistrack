import type { ReactNode } from 'react'
import { toast as sonnerToast, type ExternalToast } from 'sonner'
import {
  CheckCircle2,
  Dumbbell,
  Flame,
  OctagonAlert,
  X,
} from 'lucide-react'
import { cn } from '@/shared/lib/cn'

export type NotifyVariant = 'error' | 'success' | 'info'

const DURATION_MS = 4000

const TITLES: Record<NotifyVariant, string> = {
  error: 'Set interrupted',
  success: 'Nice work',
  info: 'Coach note',
}

type CardProps = {
  id: string | number
  variant: NotifyVariant
  title: string
  message: string
}

function Icon({ variant }: { variant: NotifyVariant }) {
  const className = 'size-5 shrink-0'
  if (variant === 'success') return <CheckCircle2 className={cn(className, 'text-emerald-700')} aria-hidden />
  if (variant === 'info') return <Dumbbell className={cn(className, 'text-sky-700')} aria-hidden />
  return <OctagonAlert className={cn(className, 'text-orange-700')} aria-hidden />
}

export function WorkoutToastCard({ id, variant, title, message }: CardProps) {
  return (
    <div
      role={variant === 'error' ? 'alert' : 'status'}
      className={cn(
        'relative w-[min(calc(100vw-1.5rem),22rem)] overflow-hidden rounded-2xl border shadow-lg backdrop-blur-md',
        'bg-stone-50/95 text-stone-900',
        variant === 'error' && 'border-orange-300/80',
        variant === 'success' && 'border-emerald-300/80',
        variant === 'info' && 'border-sky-300/70',
      )}
    >
      <div
        className={cn(
          'pointer-events-none absolute inset-y-0 left-0 w-1.5',
          variant === 'error' && 'bg-linear-to-b from-orange-500 to-amber-600',
          variant === 'success' && 'bg-linear-to-b from-emerald-500 to-teal-600',
          variant === 'info' && 'bg-linear-to-b from-sky-500 to-cyan-600',
        )}
        aria-hidden
      />

      <div className="flex items-start gap-3 px-4 py-3.5 pl-5">
        <div
          className={cn(
            'mt-0.5 flex size-9 shrink-0 items-center justify-center rounded-xl',
            variant === 'error' && 'bg-orange-100 text-orange-800',
            variant === 'success' && 'bg-emerald-100 text-emerald-900',
            variant === 'info' && 'bg-sky-100 text-sky-900',
          )}
        >
          <Icon variant={variant} />
        </div>

        <div className="min-w-0 flex-1 pt-0.5">
          <div className="flex items-center gap-1.5">
            <p className="text-sm font-bold tracking-tight">{title}</p>
            {variant === 'success' ? (
              <Flame className="size-3.5 text-orange-500" aria-hidden />
            ) : null}
          </div>
          <p className="mt-0.5 text-sm leading-snug text-stone-600">{message}</p>
        </div>

        <button
          type="button"
          onClick={() => sonnerToast.dismiss(id)}
          className="inline-flex size-8 shrink-0 items-center justify-center rounded-lg text-stone-400 transition hover:bg-stone-100 hover:text-stone-700"
          aria-label="Dismiss"
        >
          <X className="size-4" aria-hidden />
        </button>
      </div>

      <div
        className={cn(
          'h-0.5 origin-left animate-[toast-progress_var(--toast-duration)_linear_forwards]',
          variant === 'error' && 'bg-orange-400/80',
          variant === 'success' && 'bg-emerald-500/80',
          variant === 'info' && 'bg-sky-500/80',
        )}
        style={{ ['--toast-duration' as string]: `${DURATION_MS}ms` }}
        aria-hidden
      />
    </div>
  )
}

type NotifyOptions = ExternalToast & {
  title?: string
}

function show(
  variant: NotifyVariant,
  message: ReactNode,
  options?: NotifyOptions,
) {
  const text =
    typeof message === 'string'
      ? message
      : message == null
        ? ''
        : String(message)

  const title = options?.title ?? TITLES[variant]
  const { title: _t, duration, ...rest } = options ?? {}

  // Unique id every call so the same message (e.g. repeated 401) still pops again
  return sonnerToast.custom(
    (id) => (
      <WorkoutToastCard
        id={id}
        variant={variant}
        title={title}
        message={text || (variant === 'error' ? 'Something went sideways mid-set.' : 'Done.')}
      />
    ),
    {
      duration: duration ?? DURATION_MS,
      id: `${variant}-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
      ...rest,
    },
  )
}

/**
 * Drop-in workout-themed toasts. Prefer this over raw `sonner` for UI consistency.
 * Auto-dismisses in ~4s.
 */
export const toast = Object.assign(
  (message: ReactNode, options?: NotifyOptions) => show('info', message, options),
  {
    success: (message: ReactNode, options?: NotifyOptions) =>
      show('success', message, options),
    error: (message: ReactNode, options?: NotifyOptions) =>
      show('error', message, options),
    message: (message: ReactNode, options?: NotifyOptions) =>
      show('info', message, options),
    info: (message: ReactNode, options?: NotifyOptions) =>
      show('info', message, options),
    dismiss: sonnerToast.dismiss.bind(sonnerToast),
    promise: sonnerToast.promise.bind(sonnerToast),
  },
)

export const TOAST_DURATION_MS = DURATION_MS
