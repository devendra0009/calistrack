import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { Toaster } from 'sonner'
import { AuthSessionProvider } from '@/features/auth/AuthSessionProvider'
import { router } from '@/app/router'
import { ThemeProvider, useTheme } from '@/shared/theme/ThemeProvider'
import { WorkoutMusicProvider } from '@/features/workout-music/WorkoutMusicProvider'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
})

function ThemedToaster() {
  const { theme } = useTheme()
  return <Toaster richColors position="top-center" theme={theme} />
}

export function AppProviders() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthSessionProvider>
          <WorkoutMusicProvider>
            <RouterProvider router={router} />
            <ThemedToaster />
          </WorkoutMusicProvider>
        </AuthSessionProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
