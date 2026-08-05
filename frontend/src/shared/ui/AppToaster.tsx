import { Toaster } from 'sonner'
import { useTheme } from '@/shared/theme/ThemeProvider'
import { TOAST_DURATION_MS } from '@/shared/ui/notify'

/** Sonner host — cards themselves are rendered via `toast` in notify.tsx */
export function AppToaster() {
  const { theme } = useTheme()

  return (
    <Toaster
      theme={theme}
      position="top-center"
      duration={TOAST_DURATION_MS}
      gap={12}
      visibleToasts={4}
      expand={false}
      offset={{ top: 'max(0.75rem, env(safe-area-inset-top))' }}
      toastOptions={{
        unstyled: true,
        classNames: {
          toast: 'bg-transparent border-0 shadow-none p-0',
        },
      }}
    />
  )
}
