import { Moon, Sun } from 'lucide-react'
import { useTheme } from '@/shared/theme/ThemeProvider'
import { cn } from '@/shared/lib/cn'

export function ThemeToggle({ className }: { className?: string }) {
  const { theme, toggleTheme } = useTheme()
  const isDark = theme === 'dark'

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={cn(
        'inline-flex size-9 shrink-0 cursor-pointer items-center justify-center rounded-lg border border-stone-300 bg-stone-50 text-stone-700 transition hover:bg-stone-100',
        className,
      )}
      aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
      title={isDark ? 'Light mode' : 'Dark mode'}
    >
      {isDark ? (
        <Sun className="size-4" aria-hidden />
      ) : (
        <Moon className="size-4" aria-hidden />
      )}
    </button>
  )
}
