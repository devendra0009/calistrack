import { useEffect, useRef, useState } from 'react'
import { Headphones, Pause, Play } from 'lucide-react'
import { cn } from '@/shared/lib/cn'
import { WORKOUT_MOODS } from '@/shared/lib/workout-music'
import { useWorkoutMusic } from '@/features/workout-music/WorkoutMusicProvider'

export function WorkoutMusicControl({ className }: { className?: string }) {
  const { mood, setMood, isPlaying, toggle, inWorkout } = useWorkoutMusic()
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const onPointer = (e: MouseEvent | TouchEvent) => {
      const el = rootRef.current
      if (!el) return
      if (e.target instanceof Node && !el.contains(e.target)) {
        setOpen(false)
      }
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onPointer)
    document.addEventListener('touchstart', onPointer)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onPointer)
      document.removeEventListener('touchstart', onPointer)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  const activeMood = WORKOUT_MOODS.find((m) => m.id === mood) ?? WORKOUT_MOODS[0]!
  const accent = isPlaying || inWorkout

  return (
    <div ref={rootRef} className={cn('relative shrink-0', className)}>
      <div
        className={cn(
          'inline-flex h-9 items-center overflow-hidden rounded-lg border border-stone-300 bg-stone-50 text-stone-700',
          accent && 'border-emerald-300 bg-emerald-50 text-emerald-900',
        )}
      >
        <button
          type="button"
          onClick={() => void toggle()}
          className="inline-flex size-9 shrink-0 items-center justify-center transition hover:bg-stone-100/80"
          aria-label={isPlaying ? 'Pause music' : 'Play music'}
          title={isPlaying ? 'Pause' : 'Play'}
        >
          {isPlaying ? (
            <Pause className="size-4" aria-hidden />
          ) : (
            <Play className="size-4" aria-hidden />
          )}
        </button>

        <span className="h-5 w-px shrink-0 bg-stone-300" aria-hidden />

        <button
          type="button"
          onClick={() => setOpen((v) => !v)}
          className="inline-flex h-9 items-center gap-1 px-2 transition hover:bg-stone-100/80"
          aria-expanded={open}
          aria-haspopup="dialog"
          aria-label={`Mood: ${activeMood.label}`}
          title="Choose mood"
        >
          <Headphones className="size-4 shrink-0" aria-hidden />
          <span className="max-w-14 truncate text-xs font-semibold sm:max-w-none">
            {activeMood.label}
          </span>
        </button>
      </div>

      {open ? (
        <div
          role="dialog"
          aria-label="Workout music mood"
          className="absolute right-0 z-40 mt-1.5 w-44 rounded-xl border border-stone-200 bg-stone-50 p-2 shadow-lg"
        >
          <p className="mb-1 px-1 text-[10px] font-semibold uppercase tracking-wide text-stone-500">
            Mood
          </p>
          <div className="grid grid-cols-2 gap-1">
            {WORKOUT_MOODS.map((m) => {
              const selected = m.id === mood
              return (
                <button
                  key={m.id}
                  type="button"
                  onClick={() => {
                    setMood(m.id)
                  }}
                  className={cn(
                    'rounded-lg px-2 py-1.5 text-xs font-semibold transition',
                    selected
                      ? 'bg-emerald-100 text-emerald-900'
                      : 'text-stone-700 hover:bg-stone-100',
                  )}
                >
                  {m.label}
                </button>
              )
            })}
          </div>
        </div>
      ) : null}
    </div>
  )
}
