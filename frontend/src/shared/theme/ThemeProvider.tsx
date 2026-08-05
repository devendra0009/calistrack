import {
  createContext,
  use,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import {
  getTheme,
  initTheme,
  onThemeChange,
  setTheme as applyTheme,
  toggleTheme as flipTheme,
  type ThemeId,
} from '@/shared/theme/theme'

type ThemeContextValue = {
  theme: ThemeId
  setTheme: (theme: ThemeId) => void
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextValue | null>(null)

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<ThemeId>(() =>
    typeof document !== 'undefined' ? getTheme() : 'light',
  )

  useEffect(() => {
    initTheme()
    setThemeState(getTheme())
    return onThemeChange(({ theme: next }) => setThemeState(next))
  }, [])

  const setTheme = (next: ThemeId) => {
    applyTheme(next)
  }

  const toggleTheme = () => {
    flipTheme()
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
