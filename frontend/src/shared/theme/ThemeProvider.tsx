import {
  createContext,
  use,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import {
  applyThemeClass,
  resolveInitialTheme,
  writeStoredTheme,
  type ThemePreference,
} from '@/shared/lib/theme-storage'

type ThemeContextValue = {
  theme: ThemePreference
  setTheme: (theme: ThemePreference) => void
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextValue | null>(null)

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<ThemePreference>(() =>
    typeof document !== 'undefined' ? resolveInitialTheme() : 'light',
  )

  useEffect(() => {
    applyThemeClass(theme)
    writeStoredTheme(theme)
  }, [theme])

  const setTheme = (next: ThemePreference) => {
    setThemeState(next)
  }

  const toggleTheme = () => {
    setThemeState((prev) => (prev === 'dark' ? 'light' : 'dark'))
  }

  return (
    <ThemeContext value={{ theme, setTheme, toggleTheme }}>
      {children}
    </ThemeContext>
  )
}

export function useTheme(): ThemeContextValue {
  const ctx = use(ThemeContext)
  if (!ctx) {
    throw new Error('useTheme must be used within ThemeProvider')
  }
  return ctx
}
