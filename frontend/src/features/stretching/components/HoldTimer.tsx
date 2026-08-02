import { useEffect, useRef, useState, type ReactNode } from 'react'
import { Pause, Play, RotateCcw } from 'lucide-react'
import { cn } from '@/shared/lib/cn'
import { formatHoldClock } from '@/features/stretching/lib/stretchGuide'

type HoldTimerProps = {
  /** Target hold duration in seconds. Falls back to a free-run stopwatch when null. */
  targetSeconds: number | null
  /** Reset timer when this identity changes (e.g. workout exercise id). */
  resetKey: string
  className?: string
  onComplete?: () => void
}

type Mode = 'idle' | 'running' | 'paused' | 'done'

function IconButton({
  label,
  onClick,
  variant = 'primary',
  children,
}: {
  label: string
  onClick: () => void
  variant?: 'primary' | 'secondary' | 'ghost'
  children: ReactNode
}) {
  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      onClick={onClick}
      className={cn(
        'inline-flex size-11 shrink-0 cursor-pointer touch-manipulation items-center justify-center rounded-full transition disabled:cursor-not-allowed disabled:opacity-50 sm:size-10',
        variant === 'primary' &&
          'bg-sky-700 text-white hover:bg-sky-800',
        variant === 'secondary' &&
          'border border-sky-300 bg-white text-sky-900 hover:bg-sky-50',
        variant === 'ghost' &&
          'text-sky-800 hover:bg-sky-100',
      )}
    >
      {children}
    </button>
  )
}

/**
 * Hold timer — horizontal strip on phones, compact column on larger screens.
 */
export function HoldTimer({
  targetSeconds,
  resetKey,
  className,
  onComplete,
}: HoldTimerProps) {
  const isCountdown = targetSeconds != null && targetSeconds > 0
  const initial = isCountdown ? targetSeconds : 0
  const [mode, setMode] = useState<Mode>('idle')
  const [seconds, setSeconds] = useState(initial)
  const onCompleteRef = useRef(onComplete)
  onCompleteRef.current = onComplete

  useEffect(() => {
    setMode('idle')
    setSeconds(isCountdown ? (targetSeconds ?? 0) : 0)
  }, [resetKey, isCountdown, targetSeconds])

  useEffect(() => {
    if (mode !== 'running') return
    const id = window.setInterval(() => {
      setSeconds((prev) => {
        if (isCountdown) {
          if (prev <= 1) {
            window.clearInterval(id)
            setMode('done')
            onCompleteRef.current?.()
            return 0
          }
          return prev - 1
        }
        return prev + 1
      })
    }, 1000)
    return () => window.clearInterval(id)
  }, [mode, isCountdown])

  const progress = isCountdown && targetSeconds
    ? 1 - seconds / targetSeconds
    : 0

  const statusLabel =
    mode === 'done'
      ? 'Done'
      : mode === 'running'
        ? isCountdown
          ? 'Holding'
          : 'Running'
        : mode === 'paused'
          ? 'Paused'
          : isCountdown
            ? `${targetSeconds}s`
            : 'Timer'

  const controls = (
    <div className="flex items-center justify-center gap-2">
      {mode === 'idle' || mode === 'done' ? (
        <IconButton
          label={mode === 'done' ? 'Restart' : 'Start'}
          onClick={() => {
            setSeconds(isCountdown ? (targetSeconds ?? 0) : 0)
            setMode('running')
          }}
        >
          <Play className="size-4 fill-current" />
        </IconButton>
      ) : null}

      {mode === 'running' ? (
        <IconButton
          label="Pause"
          variant="secondary"
          onClick={() => setMode('paused')}
        >
          <Pause className="size-4 fill-current" />
        </IconButton>
      ) : null}

      {mode === 'paused' ? (
        <IconButton label="Resume" onClick={() => setMode('running')}>
          <Play className="size-4 fill-current" />
        </IconButton>
      ) : null}

      {mode !== 'idle' ? (
        <IconButton
          label="Reset"
          variant="ghost"
          onClick={() => {
            setMode('idle')
            setSeconds(isCountdown ? (targetSeconds ?? 0) : 0)
          }}
        >
          <RotateCcw className="size-4" />
        </IconButton>
      ) : null}
    </div>
  )

  return (
    <div
      className={cn(
        'flex w-full flex-row items-center gap-3 rounded-2xl border border-sky-200 bg-linear-to-b from-sky-50 to-white px-3 py-3',
        'sm:max-w-40 sm:flex-col sm:justify-center sm:gap-0 sm:px-3 sm:py-4 sm:text-center',
        mode === 'done' && 'border-sky-400 from-sky-100',
        className,
      )}
    >
      <div className="min-w-0 flex-1 sm:flex-none sm:w-full">
        <p className="text-[10px] font-semibold uppercase tracking-wide text-sky-800">
          {statusLabel}
        </p>
        <p
          className={cn(
            'mt-0.5 font-mono text-3xl font-bold tabular-nums tracking-tight text-sky-950 sm:mt-1.5',
            mode === 'done' && 'text-sky-700',
          )}
          aria-live="polite"
        >
          {formatHoldClock(seconds)}
        </p>

        {isCountdown && targetSeconds ? (
          <div
            className="mt-2 h-1.5 w-full max-w-40 overflow-hidden rounded-full bg-sky-100 sm:mx-auto sm:mt-3 sm:max-w-none"
            role="progressbar"
            aria-valuemin={0}
            aria-valuemax={targetSeconds}
            aria-valuenow={targetSeconds - seconds}
          >
            <div
              className={cn(
                'h-full rounded-full bg-sky-600 transition-[width] duration-1000 ease-linear',
                mode === 'done' && 'bg-sky-700',
              )}
              style={{ width: `${Math.min(100, Math.max(0, progress * 100))}%` }}
            />
          </div>
        ) : null}

        {mode === 'done' ? (
          <p className="mt-1.5 hidden text-[11px] font-medium leading-snug text-sky-800 sm:mt-3 sm:block">
            Mark done when ready
          </p>
        ) : null}
      </div>

      <div className="shrink-0 sm:mt-4">{controls}</div>
    </div>
  )
}
