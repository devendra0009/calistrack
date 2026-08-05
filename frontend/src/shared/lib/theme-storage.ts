/**
 * Thin re-exports — persistence + DOM apply live in theme.ts.
 * Kept so existing imports continue to work.
 */
export {
  type ThemeId as ThemePreference,
  resolveInitialTheme,
  setTheme as applyThemeClass,
  systemPrefersDark,
} from '@/shared/theme/theme'

import { setTheme, type ThemeId } from '@/shared/theme/theme'

const THEME_KEY = 'calistrack.theme'

export function readStoredTheme(): ThemeId | null {
  try {
    const value = localStorage.getItem(THEME_KEY)
    if (value === 'light' || value === 'dark') return value
  } catch {
    /* ignore */
  }
  return null
}

export function writeStoredTheme(theme: ThemeId): void {
  try {
    localStorage.setItem(THEME_KEY, theme)
  } catch {
    /* ignore */
  }
}

/** @deprecated Prefer setTheme from theme.ts — kept for call-site compatibility. */
export function applyThemeOnly(theme: ThemeId): void {
  setTheme(theme, false)
}
