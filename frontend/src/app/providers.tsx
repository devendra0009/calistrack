import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router'
import { AuthSessionProvider } from '@/features/auth/AuthSessionProvider'
import { router } from '@/app/router'
import { ThemeProvider } from '@/shared/theme/ThemeProvider'
import { WorkoutMusicProvider } from '@/features/workout-music/WorkoutMusicProvider'
import { AppToaster } from '@/shared/ui/AppToaster'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
})

export function AppProviders() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <AuthSessionProvider>
          <WorkoutMusicProvider>
            <RouterProvider router={router} />
            <AppToaster />
          </WorkoutMusicProvider>
        </AuthSessionProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
