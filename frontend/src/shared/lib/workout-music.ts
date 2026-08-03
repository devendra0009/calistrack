/**
 * Bundled workout lofi tracks.
 * Drop MP3s here: `frontend/public/sounds/lofi/*.mp3`
 */

export type WorkoutMoodId = 'chill' | 'focus' | 'groove' | 'energetic'

export type WorkoutMood = {
  id: WorkoutMoodId
  label: string
  /** Hint for which track vibe to download */
  hint: string
  file: string
}

export const WORKOUT_MOODS: readonly WorkoutMood[] = [
  {
    id: 'chill',
    label: 'Chill',
    hint: 'Classic dusty lofi hip-hop, relaxed BPM',
    file: 'chill.mp3',
  },
  {
    id: 'focus',
    label: 'Focus',
    hint: 'Deeper, minimal beats — less melody, steady loop',
    file: 'focus.mp3',
  },
  {
    id: 'groove',
    label: 'Groove',
    hint: 'Slightly punchier / rhythmic for training energy',
    file: 'groove.mp3',
  },
  {
    id: 'energetic',
    label: 'Energetic',
    hint: 'Ambient / dreamy — good for stretch & training hard',
    file: 'energetic.mp3',
  },
] as const

export const DEFAULT_WORKOUT_MOOD: WorkoutMoodId = 'chill'

function trackSrc(file: string): string {
  const base = import.meta.env.BASE_URL || '/'
  const prefix = base.endsWith('/') ? base : `${base}/`
  return `${prefix}sounds/lofi/${file}`
}

export function moodById(id: WorkoutMoodId): WorkoutMood {
  return WORKOUT_MOODS.find((m) => m.id === id) ?? WORKOUT_MOODS[0]!
}

let audio: HTMLAudioElement | null = null
let currentMood: WorkoutMoodId | null = null

function getAudio(): HTMLAudioElement {
  if (!audio) {
    audio = new Audio()
    audio.loop = true
    audio.preload = 'auto'
    audio.volume = 0.55
  }
  return audio
}

export function preloadWorkoutMusic(mood: WorkoutMoodId = DEFAULT_WORKOUT_MOOD): void {
  if (typeof window === 'undefined') return
  const el = getAudio()
  const next = moodById(mood)
  const src = trackSrc(next.file)
  if (el.src !== new URL(src, window.location.origin).href) {
    el.src = src
    currentMood = mood
    try {
      el.load()
    } catch {
      /* ignore */
    }
  }
}

export async function playWorkoutMusic(mood: WorkoutMoodId): Promise<boolean> {
  if (typeof window === 'undefined') return false
  const el = getAudio()
  const next = moodById(mood)
  const src = trackSrc(next.file)

  if (currentMood !== mood || !el.src) {
    el.src = src
    currentMood = mood
    try {
      el.load()
    } catch {
      /* ignore */
    }
  }

  try {
    await el.play()
    return true
  } catch {
    return false
  }
}

export function pauseWorkoutMusic(): void {
  if (!audio) return
  audio.pause()
}

export function isWorkoutMusicPlaying(): boolean {
  return Boolean(audio && !audio.paused && !audio.ended)
}
