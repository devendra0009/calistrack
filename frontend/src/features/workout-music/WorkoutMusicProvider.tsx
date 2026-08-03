import {
  createContext,
  use,
  useCallback,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import {
  DEFAULT_WORKOUT_MOOD,
  isWorkoutMusicPlaying,
  pauseWorkoutMusic,
  playWorkoutMusic,
  preloadWorkoutMusic,
  type WorkoutMoodId,
} from '@/shared/lib/workout-music'
import {
  readStoredMood,
  readStoredMusicEnabled,
  writeStoredMood,
  writeStoredMusicEnabled,
} from '@/shared/lib/workout-music-storage'

type WorkoutMusicContextValue = {
  mood: WorkoutMoodId
  setMood: (mood: WorkoutMoodId) => void
  /** User wants music while a workout is active */
  enabled: boolean
  isPlaying: boolean
  /** True after Start/Continue until Finish (or explicit leave) */
  inWorkout: boolean
  play: () => Promise<void>
  pause: () => void
  toggle: () => Promise<void>
  /** Call from Start / Continue training (user gesture → autoplay OK) */
  enterWorkout: () => void
  leaveWorkout: () => void
}

const WorkoutMusicContext = createContext<WorkoutMusicContextValue | null>(null)

export function WorkoutMusicProvider({ children }: { children: ReactNode }) {
  const [mood, setMoodState] = useState<WorkoutMoodId>(
    () => readStoredMood() ?? DEFAULT_WORKOUT_MOOD,
  )
  const [enabled, setEnabled] = useState(() => readStoredMusicEnabled())
  const [isPlaying, setIsPlaying] = useState(false)
  const [inWorkout, setInWorkout] = useState(false)

  useEffect(() => {
    preloadWorkoutMusic(mood)
  }, [mood])

  useEffect(() => {
    const sync = () => setIsPlaying(isWorkoutMusicPlaying())
    const el = typeof window !== 'undefined' ? document : null
    // Poll lightly so UI stays in sync if playback ends/fails.
    const id = window.setInterval(sync, 800)
    el?.addEventListener('visibilitychange', sync)
    return () => {
      window.clearInterval(id)
      el?.removeEventListener('visibilitychange', sync)
    }
  }, [])

  const setMood = useCallback(
    (next: WorkoutMoodId) => {
      setMoodState(next)
      writeStoredMood(next)
      if (inWorkout && enabled) {
        void playWorkoutMusic(next).then((ok) => setIsPlaying(ok))
      } else {
        preloadWorkoutMusic(next)
      }
    },
    [enabled, inWorkout],
  )

  const play = useCallback(async () => {
    setEnabled(true)
    writeStoredMusicEnabled(true)
    const ok = await playWorkoutMusic(mood)
    setIsPlaying(ok)
  }, [mood])

  const pause = useCallback(() => {
    pauseWorkoutMusic()
    setIsPlaying(false)
    setEnabled(false)
    writeStoredMusicEnabled(false)
  }, [])

  const toggle = useCallback(async () => {
    if (isWorkoutMusicPlaying()) {
      pause()
      return
    }
    await play()
  }, [pause, play])

  const enterWorkout = useCallback(() => {
    setInWorkout(true)
    setEnabled(true)
    writeStoredMusicEnabled(true)
    void playWorkoutMusic(mood).then((ok) => setIsPlaying(ok))
  }, [mood])

  const leaveWorkout = useCallback(() => {
    setInWorkout(false)
    pauseWorkoutMusic()
    setIsPlaying(false)
  }, [])

  return (
    <WorkoutMusicContext
      value={{
        mood,
        setMood,
        enabled,
        isPlaying,
        inWorkout,
        play,
        pause,
        toggle,
        enterWorkout,
        leaveWorkout,
      }}
    >
      {children}
    </WorkoutMusicContext>
  )
}

export function useWorkoutMusic(): WorkoutMusicContextValue {
  const ctx = use(WorkoutMusicContext)
  if (!ctx) {
    throw new Error('useWorkoutMusic must be used within WorkoutMusicProvider')
  }
  return ctx
}
