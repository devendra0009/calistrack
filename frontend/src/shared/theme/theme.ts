/** Theme + lofi-driven mood. Mood IDs match workout music: chill|focus|groove|energetic */

export type ThemeId = 'light' | 'dark'
export type MoodId = 'chill' | 'focus' | 'groove' | 'energetic'

export const THEMES = ['light', 'dark'] as const
export const MOODS = ['chill', 'focus', 'groove', 'energetic'] as const

const TK = 'calistrack.theme'
const MK = 'calistrack.mood'
const MUSIC_MOOD_KEY = 'calistrack.workoutMusic.mood'
const EV = 'calistrack:theme'

/** Legacy visual moods → lofi labels */
const LEGACY: Record<string, MoodId> = {
  energy: 'groove',
  night: 'energetic',
}

const ls = {
  get: (k: string) => {
    try {
      return localStorage.getItem(k)
    } catch {
      return null
    }
  },
  set: (k: string, v: string) => {
    try {
      localStorage.setItem(k, v)
    } catch {
      /* */
    }
  },
}

const isMood = (v: string | null | undefined): v is MoodId =>
  v === 'chill' || v === 'focus' || v === 'groove' || v === 'energetic'

const isTheme = (v: string | null | undefined): v is ThemeId =>
  v === 'light' || v === 'dark'

function normalizeMood(raw: string | null | undefined): MoodId | null {
  if (!raw) return null
  const key = raw.trim().toLowerCase()
  if (isMood(key)) return key
  return LEGACY[key] ?? null
}

function emit() {
  window.dispatchEvent(
    new CustomEvent(EV, { detail: { theme: getTheme(), mood: getMood() } }),
  )
}

export function systemPrefersDark() {
  return matchMedia('(prefers-color-scheme: dark)').matches
}

export function resolveInitialTheme(): ThemeId {
  const s = ls.get(TK)
  return isTheme(s) ? s : systemPrefersDark() ? 'dark' : 'light'
}

export function getTheme(): ThemeId {
  const a = document.documentElement.getAttribute('data-theme')
  if (isTheme(a)) return a
  return document.documentElement.classList.contains('dark') ? 'dark' : 'light'
}

export function getMood(): MoodId | null {
  return normalizeMood(document.body.getAttribute('data-mood'))
}

export function setTheme(theme: ThemeId, persist = true) {
  const el = document.documentElement
  el.setAttribute('data-theme', theme)
  el.classList.toggle('dark', theme === 'dark')
  if (persist) ls.set(TK, theme)
  emit()
}

export function toggleTheme(): ThemeId {
  const n: ThemeId = getTheme() === 'dark' ? 'light' : 'dark'
  setTheme(n)
  return n
}

export function setMood(mood: MoodId | null, persist = true) {
  if (mood) document.body.setAttribute('data-mood', mood)
  else document.body.removeAttribute('data-mood')
  if (persist && mood) ls.set(MK, mood)
  emit()
}

/** Sync visual mood from lofi track / music-player label. */
export function setMoodFromTrack(tag: string): MoodId | null {
  const m = normalizeMood(tag)
  if (!m) return null
  setMood(m)
  return m
}

export function initTheme() {
  setTheme(resolveInitialTheme(), false)
  const fromMusic = normalizeMood(ls.get(MUSIC_MOOD_KEY))
  const fromMood = normalizeMood(ls.get(MK))
  const mood = fromMusic ?? fromMood
  if (mood) setMood(mood, false)
  else emit()
  return { theme: getTheme(), mood: getMood() }
}

export function onThemeChange(
  cb: (s: { theme: ThemeId; mood: MoodId | null }) => void,
) {
  const h = (e: Event) => cb((e as CustomEvent).detail)
  window.addEventListener(EV, h)
  return () => window.removeEventListener(EV, h)
}
