export type ThemePreference = 'light' | 'dark'

const THEME_KEY = 'calistrack.theme'

export function readStoredTheme(): ThemePreference | null {
  try {
    const value = localStorage.getItem(THEME_KEY)
    if (value === 'light' || value === 'dark') return value
  } catch {
    /* ignore */
  }
  return null
}

export function writeStoredTheme(theme: ThemePreference): void {
  try {
    localStorage.setItem(THEME_KEY, theme)
  } catch {
    /* ignore */
  }
}

export function systemPrefersDark(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export function resolveInitialTheme(): ThemePreference {
  return readStoredTheme() ?? (systemPrefersDark() ? 'dark' : 'light')
}

export function applyThemeClass(theme: ThemePreference): void {
  document.documentElement.classList.toggle('dark', theme === 'dark')
}
