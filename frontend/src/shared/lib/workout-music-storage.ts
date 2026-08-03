import type { WorkoutMoodId } from '@/shared/lib/workout-music'

const MOOD_KEY = 'calistrack.workoutMusic.mood'
const ENABLED_KEY = 'calistrack.workoutMusic.enabled'

export function readStoredMood(): WorkoutMoodId | null {
  try {
    const value = localStorage.getItem(MOOD_KEY)
    if (
      value === 'chill' ||
      value === 'focus' ||
      value === 'groove' ||
      value === 'energetic'
    ) {
      return value
    }
  } catch {
    /* ignore */
  }
  return null
}

export function writeStoredMood(mood: WorkoutMoodId): void {
  try {
    localStorage.setItem(MOOD_KEY, mood)
  } catch {
    /* ignore */
  }
}

/** Whether the user wants music on when a workout is active (default true). */
export function readStoredMusicEnabled(): boolean {
  try {
    const value = localStorage.getItem(ENABLED_KEY)
    if (value === '0') return false
    if (value === '1') return true
  } catch {
    /* ignore */
  }
  return true
}

export function writeStoredMusicEnabled(enabled: boolean): void {
  try {
    localStorage.setItem(ENABLED_KEY, enabled ? '1' : '0')
  } catch {
    /* ignore */
  }
}
